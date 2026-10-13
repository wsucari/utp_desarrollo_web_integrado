package pe.edu.inudi.web;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.inudi.service.*;

@Controller
@RequiredArgsConstructor
public class PortalController {
  private final AcademicService academic;

  @GetMapping({"/panel", "/dashboard", "/profesor/dashboard", "/users/panel/inicio"})
  public String dashboard(Model m) {
    m.addAllAttributes(academic.dashboard());
    m.addAttribute("pageTitle", "Inicio");
    return "dashboard";
  }

  @GetMapping({"/perfil", "/profesor/perfil", "/users/panel/perfil"})
  public String profile(Model m) {
    m.addAttribute("pageTitle", "Mi perfil");
    return "profile";
  }

  @PostMapping("/perfil")
  public String profile(
      @RequestParam String fullName,
      @RequestParam(defaultValue = "") String phone,
      @RequestParam(defaultValue = "") String professionalTitle,
      @RequestParam(defaultValue = "") String oldPassword,
      @RequestParam(defaultValue = "") String newPassword,
      RedirectAttributes flash) {
    academic.profile(fullName, phone, professionalTitle, oldPassword, newPassword);
    flash.addFlashAttribute("success", "Perfil actualizado.");
    return "redirect:/perfil";
  }

  @GetMapping({"/mis-cursos", "/users/panel/inscritos"})
  public String myCourses(Model m) {
    m.addAttribute("myCourses", academic.myCourses());
    m.addAttribute("pageTitle", "Cursos Inscritos");
    return "my-courses";
  }

  @GetMapping({"/carrito", "/users/carrito"})
  public String cart(Model m) {
    var cart = academic.cart();
    m.addAttribute("cart", cart);
    m.addAttribute(
        "total",
        cart.stream().map(i -> i.getCourse().getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add));
    m.addAttribute("pageTitle", "Carrito");
    return "cart";
  }

  @PostMapping("/carrito/{courseId}")
  public String add(@PathVariable Long courseId, RedirectAttributes f) {
    academic.addCart(courseId);
    f.addFlashAttribute("success", "Curso agregado a tu selección.");
    return "redirect:/carrito";
  }

  @PostMapping("/carrito/{courseId}/retirar")
  public String remove(@PathVariable Long courseId) {
    academic.removeCart(courseId);
    return "redirect:/carrito";
  }

  @PostMapping("/carrito/solicitar")
  public String checkout(RedirectAttributes f) {
    academic.checkout();
    f.addFlashAttribute(
        "success",
        "Solicitud registrada. La administración validará el pago y habilitará tu matrícula.");
    return "redirect:/mis-pagos";
  }

  @GetMapping("/mis-pagos")
  public String payments(Model m) {
    m.addAttribute("payments", academic.myPayments());
    m.addAttribute("pageTitle", "Mis solicitudes y pagos");
    return "my-payments";
  }

  @GetMapping("/certificados")
  public String certificates(Model m) {
    m.addAttribute("certificates", academic.myCertificates());
    m.addAttribute("myCourses", academic.myCourses());
    m.addAttribute("pageTitle", "Mis logros");
    return "certificates";
  }

  @PostMapping("/certificados/emitir/{courseId}")
  public String issue(@PathVariable Long courseId, RedirectAttributes f) {
    academic.issue(courseId);
    f.addFlashAttribute("success", "Tu certificado está disponible para descargar y validar.");
    return "redirect:/certificados";
  }
}
