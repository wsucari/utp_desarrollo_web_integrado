package pe.edu.inudi.api;

import static pe.edu.inudi.api.ApiViews.map;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.*;

/** Adaptadores HTTP/JSON de los mismos casos de uso utilizados por MVC/JSP. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional
public class ApiController {
  private final AcademicService academic;
  private final CatalogService catalog;
  private final SessionService sessions;
  private final StorageService storage;
  private final ApiViews views;
  private final EntityManager entityManager;

  private Object read(Object value) {
    return views.value(value);
  }

  private Object saved(Object value) {
    entityManager.flush();
    return read(value);
  }

  private ResponseEntity<?> created(Object value) {
    return ResponseEntity.status(201).body(saved(value));
  }

  private ResponseEntity<Void> done() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/cursos")
  public Object catalog(
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String ruta,
      @RequestParam(defaultValue = "") String categoria,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "") String nivel,
      @RequestParam(defaultValue = "todos") String acceso) {
    return read(
        catalog.view(
            search.isBlank() ? q : search,
            ruta,
            categoria.isBlank() ? category : categoria,
            nivel,
            acceso));
  }

  @GetMapping("/cursos/{id}")
  public Object course(@PathVariable Long id) {
    var c = academic.course(id);
    var a = academic.current();
    if (!c.isPublished() && !academic.canManage(c, a))
      throw new AccessDeniedException("El curso no está publicado.");
    return read(
        map(
            "course",
            c,
            "syllabus",
            academic.syllabus(id),
            "canLearn",
            academic.hasAccess(c, a),
            "canManage",
            academic.canManage(c, a),
            "reviews",
            academic.courseReviews(id)));
  }

  @GetMapping("/panel")
  public Object dashboard() {
    return read(academic.dashboard());
  }

  @GetMapping("/perfil")
  public Object profile() {
    return read(academic.required());
  }

  public record Profile(
      @NotBlank @Size(max = 120) String fullName,
      @Size(max = 30) String phone,
      @Size(max = 180) String professionalTitle,
      @Size(max = 100) String oldPassword,
      @Size(max = 100) String newPassword) {}

  @PutMapping("/perfil")
  public Object profile(@Valid @RequestBody Profile f) {
    academic.profile(
        f.fullName(), f.phone(), f.professionalTitle(), f.oldPassword(), f.newPassword());
    return saved(academic.required());
  }

  @GetMapping("/mis-cursos")
  public Object myCourses() {
    return read(academic.myCourses());
  }

  @GetMapping("/mis-pagos")
  public Object myPayments() {
    return read(academic.myPayments());
  }

  @GetMapping("/carrito")
  public Object cart() {
    var items = academic.cart();
    return read(
        map(
            "cart",
            items,
            "total",
            items.stream()
                .map(i -> i.getCourse().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
  }

  @PostMapping("/carrito/{id}")
  public ResponseEntity<Void> addCart(@PathVariable Long id) {
    academic.addCart(id);
    return done();
  }

  @DeleteMapping("/carrito/{id}")
  public ResponseEntity<Void> removeCart(@PathVariable Long id) {
    academic.removeCart(id);
    return done();
  }

  @PostMapping("/carrito/solicitar")
  public ResponseEntity<Void> checkout() {
    academic.checkout();
    return done();
  }

  @GetMapping("/certificados")
  public Object certificates() {
    return read(map("certificates", academic.myCertificates(), "myCourses", academic.myCourses()));
  }

  @PostMapping("/certificados/emitir/{id}")
  public Object issue(@PathVariable Long id) {
    return saved(academic.issue(id));
  }

  @GetMapping({"/cert", "/cert/{code}"})
  public Object verify(
      @PathVariable(required = false) String code, @RequestParam(required = false) String codigo) {
    academic.student();
    var query = code == null ? codigo : code;
    return read(map("searched", query != null, "certificate", academic.verify(query).orElse(null)));
  }

  @GetMapping("/comentarios")
  public Object comments() {
    return read(academic.myComments());
  }

  @GetMapping("/admin/usuarios")
  public Object users(@RequestParam(defaultValue = "") String role) {
    return read(
        map(
            "accounts",
            academic.allAccounts().stream()
                .filter(a -> role.isBlank() || a.getRole().name().equals(role))
                .toList()));
  }

  @GetMapping("/admin/profesores")
  public Object teachers() {
    return users("DOCENTE");
  }

  @GetMapping("/admin/usuarios/{id}")
  public Object user(@PathVariable Long id) {
    academic.admin();
    return read(academic.account(id));
  }

  @PostMapping("/admin/usuarios")
  public ResponseEntity<?> createUser(@Valid @RequestBody AccountForm f) {
    return created(academic.saveAccount(null, f));
  }

  @PutMapping("/admin/usuarios/{id}")
  public Object updateUser(@PathVariable Long id, @Valid @RequestBody AccountForm f) {
    return saved(academic.saveAccount(id, f));
  }

  @GetMapping({"/admin/cursos", "/gestion/cursos"})
  public Object courses() {
    return read(map("courses", academic.managedCourses()));
  }

  @PostMapping("/admin/cursos")
  public ResponseEntity<?> createCourse(@Valid @RequestBody CourseForm f) {
    return created(academic.saveCourse(null, f));
  }

  @PutMapping("/admin/cursos/{id}")
  public Object updateCourse(@PathVariable Long id, @Valid @RequestBody CourseForm f) {
    return saved(academic.saveCourse(id, f));
  }

  @DeleteMapping("/admin/cursos/{id}")
  public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
    academic.deleteCourse(id);
    return done();
  }

  @GetMapping("/admin/opciones")
  public Object options() {
    return read(
        map(
            "teachers",
            academic.teachers(),
            "students",
            academic.students(),
            "courses",
            academic.managedCourses(),
            "expiration",
            LocalDate.now().plusYears(1)));
  }

  @GetMapping("/admin/pagos")
  public Object payments() {
    var dates = new LinkedHashMap<Long, LocalDate>();
    var enrollments = academic.allEnrollments();
    for (var p : academic.allPayments()) {
      var date = p.getExpiresAt();
      if (date == null)
        date =
            enrollments.stream()
                .filter(
                    e ->
                        e.getStudent().getId().equals(p.getStudent().getId())
                            && e.getCourse().getId().equals(p.getCourse().getId()))
                .map(Enrollment::getExpiresAt)
                .findFirst()
                .orElse(null);
      if (date != null) dates.put(p.getId(), date.toLocalDate());
    }
    return read(map("payments", academic.allPayments(), "paymentDates", dates));
  }

  @PostMapping("/admin/pagos")
  public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentForm f) {
    return created(academic.savePayment(null, f));
  }

  @PutMapping("/admin/pagos/{id}")
  public Object updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentForm f) {
    return saved(academic.savePayment(id, f));
  }

  public record Expiration(@NotNull LocalDate expiration) {}

  @PostMapping("/admin/pagos/{id}/confirmar")
  public Object confirm(@PathVariable Long id, @Valid @RequestBody Expiration f) {
    return saved(academic.confirmPayment(id, f.expiration()));
  }

  @PostMapping("/admin/pagos/{id}/anular")
  public ResponseEntity<Void> cancel(@PathVariable Long id) {
    academic.cancelPayment(id);
    return done();
  }

  @GetMapping("/admin/matriculas")
  public Object enrollments() {
    return read(academic.allEnrollments());
  }

  @PostMapping("/admin/matriculas/{id}/cancelar")
  public ResponseEntity<Void> revoke(@PathVariable Long id) {
    academic.revokeEnrollment(id);
    return done();
  }

  @GetMapping("/admin/certificados")
  public Object allCertificates() {
    return read(academic.allCertificates());
  }

  @GetMapping("/admin/auditoria")
  public Object audit() {
    return read(academic.auditLog());
  }

  @GetMapping("/admin/resenas")
  public Object reviews() {
    return read(academic.contentReviews());
  }

  @GetMapping("/admin/sesiones")
  public Object sessions(
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") String sesion) {
    return read(sessions.list(search, sesion));
  }

  @GetMapping("/admin/vista-previa")
  public Object preview(
      @RequestParam(defaultValue = "") @Size(max = 180) String search,
      @RequestParam(defaultValue = "") String roleFilter) {
    if (search.length() > 180)
      throw new BusinessException("La búsqueda supera el tamaño permitido.");
    return read(
        academic.allAccounts().stream()
            .filter(a -> a.getRole() != Role.ADMIN)
            .filter(a -> roleFilter.isBlank() || a.getRole().name().equals(roleFilter))
            .filter(
                a ->
                    (a.getFullName() + " " + a.getEmail())
                        .toLowerCase(Locale.ROOT)
                        .contains(search.toLowerCase(Locale.ROOT)))
            .toList());
  }

  @GetMapping("/admin/vista-previa/{profile}/{id}")
  public Object preview(@PathVariable String profile, @PathVariable Long id) {
    var result = academic.previewAccount(id);
    var a = academic.account(id);
    if (!(a.getRole() == Role.DOCENTE ? "profesor" : "estudiante").equals(profile))
      throw new BusinessException("El perfil no corresponde al usuario.");
    return read(result);
  }

  @GetMapping("/gestion/cursos/{id}")
  public Object manage(@PathVariable Long id) {
    return read(map("course", academic.managed(id), "syllabus", academic.syllabus(id)));
  }

  public record ModuleForm(
      @NotBlank @Size(max = 180) String title, @Min(1) @Max(999) int position) {}

  @PostMapping("/gestion/cursos/{id}/modulos")
  public ResponseEntity<?> createModule(@PathVariable Long id, @Valid @RequestBody ModuleForm f) {
    return created(academic.saveModule(id, null, f.title(), f.position()));
  }

  @PutMapping("/gestion/cursos/{courseId}/modulos/{id}")
  public Object updateModule(
      @PathVariable Long courseId, @PathVariable Long id, @Valid @RequestBody ModuleForm f) {
    return saved(academic.saveModule(courseId, id, f.title(), f.position()));
  }

  @DeleteMapping("/gestion/modulos/{id}")
  public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
    academic.deleteModule(id);
    return done();
  }

  @GetMapping("/gestion/modulos/{id}")
  public Object module(@PathVariable Long id) {
    return read(
        academic.managedCourses().stream()
            .flatMap(c -> academic.modules(c.getId()).stream())
            .filter(m -> m.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new AccessDeniedException("El módulo no está asignado.")));
  }

  @PostMapping("/gestion/modulos/{id}/contenidos")
  public ResponseEntity<?> createContent(@PathVariable Long id, @Valid @RequestBody ContentForm f) {
    return created(academic.saveContent(id, null, f));
  }

  @PutMapping("/gestion/modulos/{moduleId}/contenidos/{id}")
  public Object updateContent(
      @PathVariable Long moduleId, @PathVariable Long id, @Valid @RequestBody ContentForm f) {
    return saved(academic.saveContent(moduleId, id, f));
  }

  @DeleteMapping("/gestion/contenidos/{id}")
  public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
    academic.deleteContent(id);
    return done();
  }

  @PostMapping("/gestion/contenidos/{id}/material")
  public Object material(@PathVariable Long id, @RequestParam MultipartFile file)
      throws java.io.IOException {
    var c = academic.content(id);
    academic.managed(c.getModule().getCourse().getId());
    var stored = storage.save(file);
    try {
      academic.attachMaterial(id, stored.key(), stored.name());
      entityManager.flush();
    } catch (RuntimeException e) {
      storage.discard(stored);
      throw e;
    }
    return views.content(c, true);
  }

  @GetMapping("/gestion/contenidos/{id}/preguntas")
  public Object questions(@PathVariable Long id) {
    var c = academic.content(id);
    academic.managed(c.getModule().getCourse().getId());
    return map(
        "content",
        views.content(c, true),
        "questions",
        academic.examQuestions(id).stream().map(q -> views.question(q, true)).toList());
  }

  @PostMapping("/gestion/contenidos/{id}/preguntas")
  public ResponseEntity<?> createQuestion(
      @PathVariable Long id, @Valid @RequestBody QuestionForm f) {
    var q = academic.saveQuestion(id, null, f);
    entityManager.flush();
    return ResponseEntity.status(201).body(views.question(q, true));
  }

  @PutMapping("/gestion/contenidos/{contentId}/preguntas/{id}")
  public Object updateQuestion(
      @PathVariable Long contentId, @PathVariable Long id, @Valid @RequestBody QuestionForm f) {
    var q = academic.saveQuestion(contentId, id, f);
    entityManager.flush();
    return views.question(q, true);
  }

  @DeleteMapping("/gestion/preguntas/{id}")
  public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
    academic.deleteQuestion(id);
    return done();
  }

  @GetMapping("/gestion/cursos/{id}/seguimiento")
  public Object report(@PathVariable Long id) {
    return read(
        map(
            "course",
            academic.managed(id),
            "roster",
            academic.roster(id),
            "submissions",
            academic.courseSubmissions(id),
            "attempts",
            academic.courseAttempts(id)));
  }

  public record Grade(
      @NotNull @DecimalMin("0") @DecimalMax("20") BigDecimal grade,
      @Size(max = 1000) String feedback) {}

  @PutMapping("/gestion/entregas/{id}/calificar")
  public Object grade(@PathVariable Long id, @Valid @RequestBody Grade f) {
    academic.grade(id, f.grade(), f.feedback());
    return saved(academic.readableSubmission(id));
  }

  @GetMapping("/aula/cursos/{id}")
  public Object classroom(@PathVariable Long id) {
    var c = academic.course(id);
    var a = academic.required();
    if (!academic.hasAccess(c, a)) throw new AccessDeniedException("La matrícula no está vigente.");
    return read(
        map(
            "course",
            c,
            "syllabus",
            academic.syllabus(id),
            "completed",
            academic.completedIds(),
            "percent",
            academic.percent(a.getId(), id),
            "canManage",
            academic.canManage(c, a)));
  }

  @GetMapping({"/contenidos/{id}", "/vista-previa/{id}"})
  public Object lesson(@PathVariable Long id) {
    var c = academic.readable(id);
    var a = academic.current();
    var course = c.getModule().getCourse();
    boolean access = academic.hasAccess(course, a);
    var result =
        map(
            "content",
            views.content(c, true),
            "course",
            course,
            "syllabus",
            academic.syllabus(course.getId()),
            "canLearn",
            access,
            "canManage",
            academic.canManage(course, a),
            "completed",
            a == null ? Set.of() : academic.completedIds(),
            "posts",
            access ? academic.posts(id) : List.of());
    if (access && c.getType() == ContentType.ACTIVIDAD)
      result.put("submissions", academic.contentSubmissions(id));
    if (access && c.getType() == ContentType.EVALUACION) {
      result.put("questions", academic.examQuestions(id));
      result.put("attempts", academic.examAttempts(id));
    }
    return read(result);
  }

  @PostMapping("/contenidos/{id}/completar")
  public ResponseEntity<Void> complete(@PathVariable Long id) {
    academic.complete(id);
    return done();
  }

  @PostMapping("/contenidos/{id}/entregar")
  public ResponseEntity<?> submit(
      @PathVariable Long id,
      @RequestParam MultipartFile file,
      @RequestParam(defaultValue = "") String comment)
      throws java.io.IOException {
    academic.student();
    academic.readable(id);
    var stored = storage.save(file);
    try {
      return created(academic.submit(id, stored.key(), stored.name(), comment));
    } catch (RuntimeException e) {
      storage.discard(stored);
      throw e;
    }
  }

  @PostMapping("/contenidos/{id}/evaluar")
  public ResponseEntity<?> evaluate(
      @PathVariable Long id, @RequestBody Map<String, List<String>> replies) {
    var form = new LinkedMultiValueMap<String, String>();
    replies.forEach(
        (k, v) -> {
          if (v == null || v.stream().anyMatch(Objects::isNull))
            throw new BusinessException("Revisa las respuestas de la evaluación.");
          form.put(k, v);
        });
    return created(academic.evaluate(id, form));
  }

  @GetMapping("/evaluaciones/intentos/{id}")
  public Object result(@PathVariable Long id) {
    return read(map("attempt", academic.attempt(id), "answers", academic.result(id)));
  }

  public record Post(@Size(max = 4000) @NotBlank String body, Long parentId) {}

  @PostMapping("/contenidos/{id}/publicaciones")
  public ResponseEntity<Void> post(@PathVariable Long id, @Valid @RequestBody Post f) {
    academic.post(id, f.parentId(), f.body());
    return ResponseEntity.status(201).build();
  }

  @PutMapping("/publicaciones/{id}")
  public ResponseEntity<Void> editPost(@PathVariable Long id, @Valid @RequestBody Post f) {
    academic.editPost(id, f.body());
    return done();
  }

  @DeleteMapping("/publicaciones/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable Long id) {
    academic.deletePost(id);
    return done();
  }

  public record Reaction(@NotBlank String type) {}

  @PostMapping("/publicaciones/{id}/reaccion")
  public ResponseEntity<Void> reaction(@PathVariable Long id, @Valid @RequestBody Reaction f) {
    academic.react(id, f.type());
    return done();
  }

  public record Rating(@Min(1) @Max(5) int score) {}

  @PostMapping("/publicaciones/{id}/valorar")
  public ResponseEntity<Void> rate(@PathVariable Long id, @Valid @RequestBody Rating f) {
    academic.rate(id, f.score());
    return done();
  }

  public record Review(@Min(1) @Max(5) int score, @NotBlank @Size(max = 2000) String body) {}

  @PostMapping("/cursos/{id}/resenas")
  public ResponseEntity<Void> review(@PathVariable Long id, @Valid @RequestBody Review f) {
    academic.review(id, f.score(), f.body());
    return done();
  }

  @DeleteMapping("/resenas/{id}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
    academic.deleteReview(id);
    return done();
  }
}
