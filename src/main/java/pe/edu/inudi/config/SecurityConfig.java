package pe.edu.inudi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.inudi.api.ApiErrors;
import pe.edu.inudi.repository.AccountRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final AccountRepository accounts;

  @Value("${app.frontend.enabled:true}")
  private boolean frontend;

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  UserDetailsService userDetailsService() {
    return email ->
        accounts
            .findByEmailIgnoreCase(email.trim())
            .map(
                a ->
                    User.withUsername(a.getEmail())
                        .password(a.getPassword())
                        .roles(a.getRole().name())
                        .disabled(!a.isActive())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales no válidas"));
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, pe.edu.inudi.service.SessionService sessions) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers("/api/auth/**", "/spa/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/cursos",
                        "/api/cursos/*",
                        "/api/contenidos/*",
                        "/api/vista-previa/*")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/gestion/**")
                    .hasAnyRole("ADMIN", "DOCENTE")
                    .requestMatchers(
                        "/api/carrito/**",
                        "/api/carrito",
                        "/api/mis-cursos",
                        "/api/mis-pagos",
                        "/api/comentarios",
                        "/api/cert",
                        "/api/cert/**",
                        "/api/certificados/emitir/**")
                    .hasRole("ESTUDIANTE")
                    .requestMatchers(HttpMethod.GET, "/api/certificados")
                    .hasRole("ESTUDIANTE")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .requestMatchers(request -> frontend && SpaWebConfig.isPage(request))
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/login",
                        "/registro",
                        "/catalogo",
                        "/catalogo/*",
                        "/vista-previa/*",
                        "/cursos",
                        "/cursos/*",
                        "/images/**",
                        "/assets/**",
                        "/error")
                    .permitAll()
                    .requestMatchers("/dashboard", "/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/profesor/**")
                    .hasRole("DOCENTE")
                    .requestMatchers("/users/**", "/cert", "/cert/**")
                    .hasRole("ESTUDIANTE")
                    .requestMatchers("/gestion/**")
                    .hasAnyRole("ADMIN", "DOCENTE")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .successHandler(
                        (request, response, authentication) -> {
                          sessions.start(request.getSession(), authentication.getName());
                          String destination =
                              authentication.getAuthorities().stream()
                                      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                                  ? "/dashboard"
                                  : authentication.getAuthorities().stream()
                                          .anyMatch(a -> a.getAuthority().equals("ROLE_DOCENTE"))
                                      ? "/profesor/dashboard"
                                      : "/users/panel/inicio";
                          response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                          response.setHeader("Location", request.getContextPath() + destination);
                        })
                    .failureUrl("/login?error")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .addLogoutHandler(
                        (request, response, authentication) ->
                            sessions.close(request.getSession(false)))
                    .logoutSuccessHandler(
                        (request, response, authentication) -> {
                          response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                          response.setHeader(
                              "Location", request.getContextPath() + "/login?salida");
                        }))
        .exceptionHandling(
            errors ->
                errors
                    .authenticationEntryPoint(
                        (request, response, exception) -> {
                          if (request
                              .getRequestURI()
                              .startsWith(request.getContextPath() + "/api/"))
                            ApiErrors.security(response, 401, "Inicia sesión para continuar.");
                          else response.sendRedirect(request.getContextPath() + "/login");
                        })
                    .accessDeniedHandler(
                        (request, response, exception) -> {
                          if (request
                              .getRequestURI()
                              .startsWith(request.getContextPath() + "/api/"))
                            ApiErrors.security(
                                response,
                                403,
                                "No tienes permiso para esta operación o falta el token CSRF.");
                          else {
                            response.setStatus(403);
                            request
                                .getRequestDispatcher("/acceso-denegado")
                                .forward(request, response);
                          }
                        }))
        .headers(
            headers ->
                headers.contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            "default-src 'self'; style-src 'self'; script-src 'self'; img-src"
                                + " 'self' data:; frame-src https://www.youtube-nocookie.com;"
                                + " form-action 'self'; base-uri 'self'; object-src 'none';"
                                + " frame-ancestors 'self'")));
    http.addFilterBefore(
        new OncePerRequestFilter() {
          @Override
          protected void doFilterInternal(
              HttpServletRequest req, HttpServletResponse res, FilterChain chain)
              throws ServletException, IOException {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
              var account = accounts.findByEmailIgnoreCase(auth.getName());
              if (account.isEmpty()
                  || !account.get().isActive()
                  || auth.getAuthorities().stream()
                      .noneMatch(g -> g.getAuthority().equals("ROLE_" + account.get().getRole()))) {
                SecurityContextHolder.clearContext();
                var session = req.getSession(false);
                if (session != null) session.invalidate();
                if (req.getRequestURI().startsWith(req.getContextPath() + "/api/"))
                  ApiErrors.security(
                      res, 401, "La cuenta o sus permisos cambiaron. Vuelve a iniciar sesión.");
                else res.sendRedirect(req.getContextPath() + "/login?sesion");
                return;
              }
              sessions.touch(req.getSession(false));
            }
            chain.doFilter(req, res);
          }
        },
        AuthorizationFilter.class);
    return http.build();
  }
}
