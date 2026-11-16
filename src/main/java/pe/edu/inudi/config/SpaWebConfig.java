package pe.edu.inudi.config;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;

/** Las rutas de negocio se conservan; Angular recibe la navegación HTML y /api los datos. */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {
  @Value("${app.frontend.enabled:true}")
  private boolean enabled;

  public static boolean isPage(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String accept = request.getHeader("Accept");
    return "GET".equals(request.getMethod())
        && accept != null
        && accept.contains("text/html")
        && !path.matches("^/(api|spa|assets|images)(/.*)?$")
        && !path.equals("/error")
        && !path.equals("/acceso-denegado")
        && !path.endsWith("/archivo")
        && !path.endsWith("/pdf")
        && !path.endsWith(".csv");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(
        new HandlerInterceptor() {
          @Override
          public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
              throws Exception {
            if (enabled && isPage(req)) {
              req.getRequestDispatcher("/spa/index.html").forward(req, res);
              return false;
            }
            return true;
          }
        });
  }
}
