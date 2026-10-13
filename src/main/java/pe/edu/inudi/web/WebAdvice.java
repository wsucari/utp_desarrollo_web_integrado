package pe.edu.inudi.web;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pe.edu.inudi.service.*;

@ControllerAdvice
@RequiredArgsConstructor
public class WebAdvice {
  private final AcademicService academic;

  @Value("${app.demo.enabled}")
  private boolean demo;

  @ModelAttribute
  public void common(Model model, HttpServletRequest request) {
    var a = academic.current();
    model.addAttribute("user", a);
    model.addAttribute("cartCount", a != null && a.getRole() == pe.edu.inudi.domain.Role.ESTUDIANTE
        ? academic.cart().size() : 0);
    model.addAttribute("role", a == null ? "VISITANTE" : a.getRole().name());
    model.addAttribute("ctx", request.getContextPath());
    model.addAttribute(
        "path", request.getRequestURI().substring(request.getContextPath().length()));
    model.addAttribute("demo", demo);
    model.addAttribute("appName", "Sistema Web Integrado para la Gestión Académica de Inudi Perú");
  }

  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public String business(BusinessException ex, Model m) {
    m.addAttribute("errorTitle", "Revisa esta operación");
    m.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String denied(AccessDeniedException ex, Model m) {
    m.addAttribute("errorTitle", "Acceso restringido");
    m.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    IllegalArgumentException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String invalid(Exception ex, Model m) {
    m.addAttribute("errorTitle", "Datos no válidos");
    m.addAttribute(
        "errorMessage",
        "Revisa los campos del formulario y los formatos de fecha, texto y números.");
    return "error";
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  public String upload(Model m) {
    m.addAttribute("errorTitle", "Archivo demasiado grande");
    m.addAttribute("errorMessage", "El tamaño máximo permitido es 10 MB.");
    return "error";
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public String conflict(Model m) {
    m.addAttribute("errorTitle", "El registro ya existe o está vinculado");
    m.addAttribute(
        "errorMessage",
        "La operación no se guardó. Revisa si existe un registro equivalente o si el elemento tiene historial asociado.");
    return "error";
  }
}
