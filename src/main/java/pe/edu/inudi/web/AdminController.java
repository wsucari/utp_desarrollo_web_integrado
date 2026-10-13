package pe.edu.inudi.web;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
  private final AcademicService academic;

  @GetMapping({"/usuarios", "/user"})
  public String users(Model m) {
    m.addAttribute("accounts", academic.allAccounts());
    m.addAttribute("pageTitle", "Usuarios");
    return "admin/users";
  }

  @GetMapping("/profesores")
  public String teachers(Model m) {
    m.addAttribute("accounts", academic.allAccounts().stream()
        .filter(a -> a.getRole() == Role.DOCENTE).toList());
    m.addAttribute("pageTitle", "Profesores");
    return "admin/users";
  }

  @GetMapping("/cursos")
  public String courses(Model m) {
    m.addAttribute("courses", academic.managedCourses());
    m.addAttribute("pageTitle", "Cursos");
    return "manage/courses";
  }

  @GetMapping({"/usuarios/nuevo", "/usuarios/{id}/editar"})
  public String editUser(@PathVariable(required = false) Long id,
      @RequestParam(required=false) Role initialRole, Model m) {
    academic.admin();
    AccountForm f = new AccountForm();
    if (id == null && initialRole != null) f.setRole(initialRole);
    if (id != null) {
      Account a = academic.account(id);
      f.setFullName(a.getFullName());
      f.setEmail(a.getEmail());
      f.setRole(a.getRole());
      f.setActive(a.isActive());
      f.setPhone(a.getPhone());
      f.setProfessionalTitle(a.getProfessionalTitle());
    }
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    m.addAttribute("roles", Role.values());
    m.addAttribute("pageTitle", id == null ? "Nuevo usuario" : "Editar usuario");
    return "admin/user-form";
  }

  @PostMapping({"/usuarios/nuevo", "/usuarios/{id}/editar"})
  public String saveUser(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") AccountForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      m.addAttribute("id", id);
      m.addAttribute("roles", Role.values());
      m.addAttribute("pageTitle", "Revisa los datos del usuario");
      return "admin/user-form";
    }
    academic.saveAccount(id, f);
    flash.addFlashAttribute("success", "Usuario guardado.");
    return "redirect:/admin/usuarios";
  }

  @GetMapping({"/cursos/nuevo", "/cursos/{id}/editar"})
  public String editCourse(@PathVariable(required = false) Long id, Model m) {
    CourseForm f = new CourseForm();
    if (id != null) {
      Course c = academic.managed(id);
      f.setTitle(c.getTitle());
      f.setDescription(c.getDescription());
      f.setObjectives(c.getObjectives());
      f.setCategory(c.getCategory());
      f.setLevel(c.getLevel());
      f.setDurationHours(c.getDurationHours());
      f.setPrice(c.getPrice());
      f.setPublished(c.isPublished());
      f.setCertificateEnabled(c.isCertificateEnabled());
      f.setTeacherIds(c.getTeachers().stream().map(Account::getId).toList());
    }
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    m.addAttribute("teachers", academic.teachers());
    m.addAttribute("pageTitle", id == null ? "Crear curso" : "Editar curso");
    return "admin/course-form";
  }

  @PostMapping({"/cursos/nuevo", "/cursos/{id}/editar"})
  public String saveCourse(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CourseForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      m.addAttribute("id", id);
      m.addAttribute("teachers", academic.teachers());
      m.addAttribute("pageTitle", "Revisa los datos del curso");
      return "admin/course-form";
    }
    Course c = academic.saveCourse(id, f);
    flash.addFlashAttribute("success", "Curso guardado.");
    return "redirect:/gestion/cursos/" + c.getId();
  }

  @PostMapping("/cursos/{id}/eliminar")
  public String deleteCourse(@PathVariable Long id, RedirectAttributes f) {
    academic.deleteCourse(id);
    f.addFlashAttribute("success", "Curso eliminado.");
    return "redirect:/gestion/cursos";
  }

  @GetMapping("/pagos")
  public String payments(Model m) {
    m.addAttribute("payments", academic.allPayments());
    java.util.Map<Long,LocalDate> dates = new java.util.LinkedHashMap<>();
    var enrollments = academic.allEnrollments();
    for (var p:academic.allPayments()) {
      var date = p.getExpiresAt();
      if (date == null && "CONFIRMADO".equals(p.getStatus())) date = enrollments.stream()
          .filter(e -> e.getStudent().getId().equals(p.getStudent().getId())
              && e.getCourse().getId().equals(p.getCourse().getId()))
          .map(Enrollment::getExpiresAt).findFirst().orElse(null);
      if (date != null) dates.put(p.getId(),date.toLocalDate());
    }
    m.addAttribute("paymentDates",dates);
    m.addAttribute("students", academic.students());
    m.addAttribute("courses", academic.managedCourses());
    m.addAttribute("expiration", LocalDate.now().plusYears(1));
    m.addAttribute("pageTitle", "Pagos");
    return "admin/payments";
  }

  private void paymentModel(Model m, Long id) {
    m.addAttribute("id", id);
    m.addAttribute("students", academic.students());
    m.addAttribute("courses", academic.managedCourses());
    m.addAttribute("pageTitle", id == null ? "Registrar pago" : "Editar pago");
  }

  @GetMapping({"/pagos/nuevo", "/pagos/{id}/editar"})
  public String editPayment(@PathVariable(required=false) Long id, Model m) {
    academic.admin();
    PaymentForm f = new PaymentForm();
    if (id != null) {
      Payment p = academic.payment(id);
      f.setStudentId(p.getStudent().getId()); f.setCourseId(p.getCourse().getId());
      f.setAmount(p.getAmount()); f.setReference(p.getReference()); f.setStatus(p.getStatus());
      f.setVersion(p.getVersion());
      var date = p.getExpiresAt();
      if (date == null) date = academic.allEnrollments().stream()
          .filter(e -> e.getStudent().getId().equals(p.getStudent().getId())
              && e.getCourse().getId().equals(p.getCourse().getId()))
          .map(Enrollment::getExpiresAt).findFirst().orElse(null);
      if (date != null) f.setExpiration(date.toLocalDate());
    }
    paymentModel(m, id); m.addAttribute("form", f);
    return "admin/payment-form";
  }

  @PostMapping({"/pagos/nuevo", "/pagos/{id}/editar"})
  public String savePayment(@PathVariable(required=false) Long id,
      @Valid @ModelAttribute("form") PaymentForm f, BindingResult errors,
      Model m, RedirectAttributes flash) {
    academic.admin();
    if (errors.hasErrors()) { paymentModel(m,id); return "admin/payment-form"; }
    academic.savePayment(id,f);
    flash.addFlashAttribute("success", id == null ? "Pago registrado." : "Pago actualizado.");
    return "redirect:/admin/pagos";
  }

  @PostMapping("/pagos")
  public String payment(
      @RequestParam Long studentId,
      @RequestParam Long courseId,
      @RequestParam BigDecimal amount,
      @RequestParam String reference,
      RedirectAttributes f) {
    academic.createPayment(studentId, courseId, amount, reference);
    f.addFlashAttribute("success", "Pago registrado como pendiente de validación.");
    return "redirect:/admin/pagos";
  }

  @PostMapping("/pagos/{id}/confirmar")
  public String confirm(
      @PathVariable Long id, @RequestParam LocalDate expiration, RedirectAttributes f) {
    academic.confirmPayment(id, expiration);
    f.addFlashAttribute(
        "success", "Pago confirmado y matrícula habilitada en una misma transacción.");
    return "redirect:/admin/pagos";
  }

  @PostMapping("/pagos/{id}/anular")
  public String cancel(@PathVariable Long id) {
    academic.cancelPayment(id);
    return "redirect:/admin/pagos";
  }

  @GetMapping("/matriculas")
  public String enrollments(Model m) {
    m.addAttribute("enrollments", academic.allEnrollments());
    m.addAttribute("pageTitle", "Matrículas y vigencia");
    return "admin/enrollments";
  }

  @PostMapping("/matriculas/{id}/cancelar")
  public String revoke(@PathVariable Long id, RedirectAttributes f) {
    academic.revokeEnrollment(id);
    f.addFlashAttribute("success", "Acceso cancelado. El historial académico se conserva.");
    return "redirect:/admin/matriculas";
  }

  @GetMapping("/certificados")
  public String certificates(Model m) {
    m.addAttribute("certificates", academic.allCertificates());
    m.addAttribute("pageTitle", "Certificados emitidos");
    return "admin/certificates";
  }

  @GetMapping("/auditoria")
  public String audit(Model m) {
    m.addAttribute("entries", academic.auditLog());
    m.addAttribute("pageTitle", "Registro de operaciones");
    return "admin/audit";
  }
}
