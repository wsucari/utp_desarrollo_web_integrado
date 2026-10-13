package pe.edu.inudi.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.inudi.domain.Role;
import pe.edu.inudi.service.*;

@Controller
@RequiredArgsConstructor
public class ReferenceNavigationController {
  private final AcademicService academic;
  private final SessionService sessions;

  @GetMapping("/admin/sesiones")
  public String sessions(Model model,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String sesion) {
    model.addAttribute("sessions", sessions.list(search, sesion));
    model.addAttribute("search", search);
    model.addAttribute("sesion", sesion);
    model.addAttribute("pageTitle", "Sesiones");
    return "admin/sessions";
  }

  @GetMapping("/profesor/cursos")
  public String teacherCourses(Model model) {
    model.addAttribute("courses", academic.managedCourses());
    model.addAttribute("pageTitle", "Mis cursos");
    return "manage/courses";
  }

  @GetMapping("/users/panel/comentarios")
  public String comments(Model model) {
    model.addAttribute("comments", academic.myComments());
    model.addAttribute("pageTitle", "Mis comentarios");
    return "comments";
  }

  @GetMapping("/admin/resenas")
  public String reviews(Model model) {
    model.addAttribute("comments", academic.contentReviews());
    model.addAttribute("pageTitle", "Reseñas");
    return "comments";
  }

  @GetMapping("/admin/vista-previa")
  public String preview(Model model,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String roleFilter) {
    if (search.length() > 180) throw new BusinessException("La búsqueda supera el tamaño permitido.");
    String query = search.toLowerCase(java.util.Locale.ROOT);
    model.addAttribute("accounts", academic.allAccounts().stream().filter(a -> a.getRole() != Role.ADMIN)
        .filter(a -> roleFilter.isBlank() || a.getRole().name().equals(roleFilter))
        .filter(a -> (a.getFullName() + " " + a.getEmail()).toLowerCase(java.util.Locale.ROOT).contains(query)).toList());
    model.addAttribute("search", search);
    model.addAttribute("roleFilter", roleFilter);
    model.addAttribute("pageTitle", "Ver como");
    return "admin/preview";
  }

  @GetMapping("/admin/vista-previa/{profile}/{id}")
  public String previewAccount(@PathVariable String profile, @PathVariable Long id, Model model) {
    model.addAllAttributes(academic.previewAccount(id));
    var account = academic.account(id);
    String expected = account.getRole() == Role.DOCENTE ? "profesor" : "estudiante";
    if (!expected.equals(profile)) throw new BusinessException("El perfil no corresponde al usuario.");
    model.addAttribute("pageTitle", "Vista previa");
    return "admin/preview-account";
  }
}
