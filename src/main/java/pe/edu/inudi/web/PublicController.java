package pe.edu.inudi.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.*;

@Controller
@RequiredArgsConstructor
public class PublicController {
  private final AcademicService academic;
  private final CatalogService catalog;

  @GetMapping("/login")
  public String login(Model m) {
    m.addAttribute("pageTitle", "Bienvenido a Inudi Perú");
    return "login";
  }

  @GetMapping("/registro")
  public String register(Model m) {
    m.addAttribute("form", new AccountForm());
    m.addAttribute("pageTitle", "Crea tu cuenta");
    return "register";
  }

  @PostMapping("/registro")
  public String register(
      @Valid @ModelAttribute("form") AccountForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      m.addAttribute("pageTitle", "Crea tu cuenta");
      return "register";
    }
    academic.register(f);
    flash.addFlashAttribute(
        "success", "Tu cuenta se creó. Inicia sesión para explorar los cursos.");
    return "redirect:/login";
  }

  @GetMapping("/")
  public String catalog(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String ruta,
      @RequestParam(defaultValue = "") String categoria,
      @RequestParam(defaultValue = "") String nivel,
      @RequestParam(defaultValue = "todos") String acceso,
      Model m) {
    m.addAllAttributes(catalog.view(search.isBlank() ? q : search, ruta,
        categoria.isBlank() ? category : categoria, nivel, acceso));
    m.addAttribute("pageTitle", "Cursos");
    return "catalog";
  }

  @GetMapping({"/catalogo", "/cursos"})
  public String catalogAlias(jakarta.servlet.http.HttpServletRequest request) {
    String query = request.getQueryString();
    return "redirect:/" + (query == null || query.isBlank() ? "" : "?" + query);
  }

  @GetMapping({"/catalogo/{id}", "/cursos/{id}"})
  public String detail(@PathVariable Long id, Model m) {
    var c = academic.course(id);
    var a = academic.current();
    if (!c.isPublished() && !academic.canManage(c, a))
      throw new AccessDeniedException("El curso no está publicado.");
    m.addAttribute("course", c);
    m.addAttribute("syllabus", academic.syllabus(id));
    m.addAttribute("canLearn", academic.hasAccess(c, a));
    m.addAttribute("reviews", academic.courseReviews(id));
    m.addAttribute("pageTitle", c.getTitle());
    return "course";
  }

  @GetMapping({"/cert", "/cert/{code}"})
  public String verify(
      @PathVariable(required = false) String code,
      @RequestParam(required = false, name = "codigo") String query,
      Model m) {
    String value = code != null ? code : query;
    m.addAttribute("searched", value != null);
    m.addAttribute("certificate", academic.verify(value).orElse(null));
    m.addAttribute("pageTitle", "Validación de certificados");
    return "verify";
  }

  @GetMapping("/acceso-denegado")
  @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
  public String denied(Model m) {
    m.addAttribute("errorTitle", "Acceso restringido");
    m.addAttribute("errorMessage", "Tu perfil no tiene permiso para abrir esta sección.");
    return "error";
  }
}
