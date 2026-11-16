package pe.edu.inudi.api;

import static pe.edu.inudi.api.ApiViews.map;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.AccountForm;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {
  private final AuthenticationManager authenticationManager;
  private final AcademicService academic;
  private final SessionService sessions;
  private final ApiViews views;

  @Value("${app.demo.enabled}")
  private boolean demo;

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 100) String password) {}

  @GetMapping("/csrf")
  public Object csrf(HttpServletRequest request) {
    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    return map("token", token.getToken(), "headerName", token.getHeaderName());
  }

  @GetMapping("/session")
  public Object session() {
    var a = academic.current();
    return map(
        "user",
        a == null ? null : views.account(a),
        "demo",
        demo,
        "cartCount",
        a != null && a.getRole() == pe.edu.inudi.domain.Role.ESTUDIANTE
            ? academic.cart().size()
            : 0);
  }

  @PostMapping("/login")
  public Object login(
      @Valid @RequestBody Login login, HttpServletRequest request, HttpServletResponse response) {
    var auth =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                login.email().trim(), login.password()));
    sessions.close(request.getSession(false));
    new ChangeSessionIdAuthenticationStrategy().onAuthentication(auth, request, response);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    new HttpSessionSecurityContextRepository().saveContext(context, request, response);
    new HttpSessionCsrfTokenRepository().saveToken(null, request, response);
    sessions.start(request.getSession(), auth.getName());
    return session();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    sessions.close(request.getSession(false));
    new SecurityContextLogoutHandler()
        .logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/registro")
  public ResponseEntity<?> register(@Valid @RequestBody AccountForm form) {
    academic.register(form);
    return ResponseEntity.status(201)
        .body(map("message", "Tu cuenta se creó. Inicia sesión para explorar los cursos."));
  }
}
