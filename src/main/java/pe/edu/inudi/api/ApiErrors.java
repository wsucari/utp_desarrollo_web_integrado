package pe.edu.inudi.api;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.core.annotation.Order;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pe.edu.inudi.service.BusinessException;
import pe.edu.inudi.service.ResourceNotFoundException;

@RestControllerAdvice(basePackages = "pe.edu.inudi.api")
@Order(-10)
public class ApiErrors {
  public record Error(int status, String message, Map<String, String> errors) {}

  public static void security(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response
        .getWriter()
        .write("{\"status\":" + status + ",\"message\":\"" + message + "\",\"errors\":{}}");
  }

  private ResponseEntity<Error> error(int status, String message) {
    return ResponseEntity.status(status).body(new Error(status, message, Map.of()));
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<Error> business(BusinessException e) {
    return error(422, e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<Error> denied(AccessDeniedException e) {
    var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName()))
      return error(401, "Inicia sesión para continuar.");
    return error(403, e.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<Error> auth() {
    return error(401, "El correo o la contraseña no son correctos, o la cuenta está inactiva.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Error> validation(MethodArgumentNotValidException e) {
    var fields = new LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(new Error(400, "Revisa los campos del formulario.", fields));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    IllegalArgumentException.class,
    MissingServletRequestParameterException.class,
    ServletRequestBindingException.class
  })
  ResponseEntity<Error> invalid() {
    return error(400, "Revisa los datos y los formatos de fecha, texto y números.");
  }

  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<Error> missing() {
    return error(404, "El registro no existe.");
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<Error> missingResource(ResourceNotFoundException exception) {
    return error(404, exception.getMessage());
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  ResponseEntity<Error> conflict() {
    return error(
        409,
        "El registro cambió o tiene información relacionada. Actualiza la pantalla antes de"
            + " continuar.");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<Error> size() {
    return error(413, "El tamaño máximo permitido es 10 MB.");
  }

  @ExceptionHandler(IOException.class)
  ResponseEntity<Error> file() {
    return error(422, "No se pudo leer o almacenar el archivo solicitado.");
  }
}
