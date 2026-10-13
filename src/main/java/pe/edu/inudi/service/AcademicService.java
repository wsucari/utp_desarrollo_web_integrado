package pe.edu.inudi.service;

import java.math.*;
import java.net.URI;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;
import pe.edu.inudi.web.form.*;

/**
 * Fachada de casos de uso académicos. Las operaciones de negocio se ejecutan en transacciones; los
 * repositorios encapsulan el acceso JPA a los datos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicService {
  private final AccountRepository accounts;
  private final CourseRepository courses;
  private final LearningModuleRepository modules;
  private final ContentRepository contents;
  private final EnrollmentRepository enrollments;
  private final PaymentRepository payments;
  private final CartItemRepository carts;
  private final ProgressRepository progress;
  private final SubmissionRepository submissions;
  private final QuestionRepository questions;
  private final ExamAttemptRepository attempts;
  private final ExamAnswerRepository answers;
  private final DiscussionRepository discussions;
  private final ReactionRepository reactions;
  private final RatingRepository ratings;
  private final CourseReviewRepository reviews;
  private final CertificateRepository certificates;
  private final AuditEntryRepository audits;
  private final PasswordEncoder encoder;
  private final ExamGrader grader;

  public Account current() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
      return null;
    return accounts
        .findByEmailIgnoreCase(auth.getName())
        .filter(Account::isActive)
        .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
  }

  public Account required() {
    var a = current();
    if (a == null) throw new AccessDeniedException("Inicia sesión para continuar.");
    return a;
  }

  public Account admin() {
    var a = required();
    if (a.getRole() != Role.ADMIN)
      throw new AccessDeniedException("Acceso exclusivo de administración.");
    return a;
  }

  public Account student() {
    var a = required();
    if (a.getRole() != Role.ESTUDIANTE)
      throw new AccessDeniedException("Esta operación corresponde al estudiante.");
    return a;
  }

  public Course course(Long id) {
    return courses.findById(id).orElseThrow(() -> new BusinessException("El curso no existe."));
  }

  public Content content(Long id) {
    return contents
        .findById(id)
        .orElseThrow(() -> new BusinessException("El contenido no existe."));
  }

  public Account account(Long id) {
    return accounts.findById(id).orElseThrow(() -> new BusinessException("El usuario no existe."));
  }

  public boolean canManage(Course c, Account a) {
    return a != null
        && a.isActive()
        && (a.getRole() == Role.ADMIN
            || (a.getRole() == Role.DOCENTE
                && c.getTeachers().stream().anyMatch(t -> t.getId().equals(a.getId()))));
  }

  public Course managed(Long id) {
    Course c = course(id);
    if (!canManage(c, required()))
      throw new AccessDeniedException("No tienes asignado este curso.");
    return c;
  }

  public boolean hasAccess(Course c, Account a) {
    return canManage(c, a)
        || (a != null
            && a.getRole() == Role.ESTUDIANTE
            && enrollments
                .findByStudentIdAndCourseId(a.getId(), c.getId())
                .map(Enrollment::isCurrent)
                .orElse(false));
  }

  public Content readable(Long id) {
    Content c = content(id);
    Course course = c.getModule().getCourse();
    if (!hasAccess(course, current()) && !(c.isPreview() && course.isPublished()))
      throw new AccessDeniedException("Este contenido requiere una matrícula vigente.");
    return c;
  }

  public Content materialForDownload(Long id) {
    Content c = content(id);
    requireAccess(c.getModule().getCourse());
    return c;
  }

  private Enrollment lockedAccess(Account a, Course c) {
    return enrollments
        .lockForStudent(a.getId(), c.getId())
        .filter(Enrollment::isCurrent)
        .orElseThrow(() -> new AccessDeniedException("La matrícula no está vigente."));
  }

  private void requireAccess(Course c) {
    if (!hasAccess(c, required()))
      throw new AccessDeniedException("No tienes acceso a este curso.");
  }

  private String text(String value, int max, String label) {
    String s = Objects.toString(value, "").trim();
    if (s.isBlank() || s.length() > max)
      throw new BusinessException(label + " es obligatorio y admite hasta " + max + " caracteres.");
    return s;
  }

  private String optional(String value, int max) {
    String s = Objects.toString(value, "").trim();
    if (s.length() > max) throw new BusinessException("El texto supera el tamaño permitido.");
    return s;
  }

  private void audit(Account actor, String action, String details) {
    AuditEntry a = new AuditEntry();
    a.setActor(actor);
    a.setAction(action);
    a.setDetails(optional(details, 1000));
    a.setCreatedAt(LocalDateTime.now());
    audits.save(a);
  }

  public List<Course> catalog(String q, String category) {
    return courses.catalog(optional(q, 180), optional(category, 100));
  }

  public List<String> categories() {
    return courses.catalog("", "").stream().map(Course::getCategory).distinct().sorted().toList();
  }

  public List<Course> managedCourses() {
    Account a = required();
    return a.getRole() == Role.ADMIN
        ? courses.findAllByOrderByIdDesc()
        : courses.assigned(a.getId());
  }

  public List<Account> allAccounts() {
    admin();
    return accounts.findAll().stream().sorted(Comparator.comparing(Account::getFullName)).toList();
  }

  public List<Account> students() {
    admin();
    return accounts.findByRoleOrderByFullName(Role.ESTUDIANTE);
  }

  public List<Account> teachers() {
    admin();
    return accounts.findByRoleOrderByFullName(Role.DOCENTE).stream()
        .filter(Account::isActive)
        .toList();
  }

  public List<LearningModule> modules(Long courseId) {
    return modules.findByCourseIdOrderByPositionAscIdAsc(courseId);
  }

  public List<Content> moduleContents(Long moduleId) {
    return contents.findByModuleIdOrderByPositionAscIdAsc(moduleId);
  }

  public List<Content> courseContents(Long courseId) {
    return contents.inCourse(courseId);
  }

  public List<Map<String, Object>> syllabus(Long courseId) {
    return modules(courseId).stream()
        .map(m -> Map.<String, Object>of("module", m, "contents", moduleContents(m.getId())))
        .toList();
  }

  public int percent(Long studentId, Long courseId) {
    long total = contents.countByModuleCourseId(courseId);
    return total == 0 ? 0 : (int) (100 * progress.completed(studentId, courseId) / total);
  }

  public Set<Long> completedIds() {
    Account a = required();
    return progress.findByStudentId(a.getId()).stream()
        .map(p -> p.getContent().getId())
        .collect(java.util.stream.Collectors.toSet());
  }

  public List<Map<String, Object>> myCourses() {
    Account a = student();
    return enrollments.findByStudentIdOrderByIdDesc(a.getId()).stream()
        .map(
            e ->
                enrollmentRow(e, a))
        .toList();
  }

  private Map<String, Object> enrollmentRow(Enrollment e, Account a) {
    Map<String,Object> row = new LinkedHashMap<>();
    row.putAll(Map.<String,Object>of(
                    "enrollment",
                    e,
                    "course",
                    e.getCourse(),
                    "percent",
                    percent(a.getId(), e.getCourse().getId())));
    var completed = completedIds();
    row.put("nextContent", courseContents(e.getCourse().getId()).stream()
        .filter(c -> !completed.contains(c.getId())).findFirst().orElse(null));
    row.put("moduleCount", modules(e.getCourse().getId()).size());
    row.put("contentCount", courseContents(e.getCourse().getId()).size());
    return row;
  }

  public List<Map<String, Object>> roster(Long courseId) {
    managed(courseId);
    return enrollments.findByCourseIdOrderByIdDesc(courseId).stream()
        .map(
            e ->
                Map.<String, Object>of(
                    "enrollment",
                    e,
                    "student",
                    e.getStudent(),
                    "percent",
                    percent(e.getStudent().getId(), courseId)))
        .toList();
  }

  public List<Enrollment> allEnrollments() {
    admin();
    return enrollments.findAllByOrderByIdDesc();
  }

  public List<Payment> allPayments() {
    admin();
    return payments.findAllByOrderByCreatedAtDesc();
  }

  public List<Payment> myPayments() {
    return payments.findByStudentIdOrderByCreatedAtDesc(student().getId());
  }

  public List<AuditEntry> auditLog() {
    admin();
    return audits.findTop50ByOrderByCreatedAtDesc();
  }

  public Map<String, Object> dashboard() {
    Account a = required();
    Map<String, Object> m = new LinkedHashMap<>();
    if (a.getRole() == Role.ADMIN) {
      m.put("users", accounts.findAll().stream().filter(Account::isActive).count());
      m.put("courseCount", courses.count());
      m.put("teacherCount", accounts.findByRoleOrderByFullName(Role.DOCENTE).size());
      m.put("paymentCount", allPayments().stream().filter(p -> "CONFIRMADO".equals(p.getStatus())).count());
      m.put("paymentTotal", allPayments().stream().filter(p -> "CONFIRMADO".equals(p.getStatus()))
          .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
      m.put("enrollmentCount", enrollments.count());
      m.put("certificateCount", certificates.count());
      m.put("recentPayments", allPayments().stream().limit(5).toList());
      m.put("courses", courses.findAllByOrderByIdDesc());
      m.put("courseRows", courses.findAllByOrderByIdDesc().stream().map(c ->
          Map.<String, Object>of("course", c,
            "enrollments", enrollments.findByCourseIdOrderByIdDesc(c.getId()).size(),
            "payments", allPayments().stream().filter(p -> "CONFIRMADO".equals(p.getStatus()) && p.getCourse().getId().equals(c.getId()))
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
            "certificates", certificates.findAll().stream().filter(x -> x.getCourse().getId().equals(c.getId())).count(),
            "comments", discussions.findAllByOrderByCreatedAtDesc().stream()
                .filter(d -> !d.isDeleted() && d.getParent() == null && d.getContent().getType() != ContentType.FORO
                  && d.getContent().getModule().getCourse().getId().equals(c.getId())).count())).toList());
    } else if (a.getRole() == Role.DOCENTE) {
      var list = managedCourses();
      m.put("courses", list);
      m.put("courseCount", list.size());
      m.put(
          "enrollmentCount",
          list.stream()
              .mapToLong(c -> enrollments.findByCourseIdOrderByIdDesc(c.getId()).size())
              .sum());
      m.put(
          "submissionCount",
          list.stream()
              .mapToLong(
                  c ->
                      submissions.inCourse(c.getId()).stream()
                          .filter(s -> s.getGrade() == null)
                          .count())
              .sum());
    } else {
      var list = myCourses();
      m.put("myCourses", list);
      m.put("courseCount", list.size());
      m.put("activeCount", list.stream().filter(r -> ((Enrollment) r.get("enrollment")).isCurrent()).count());
      m.put("completedCount", list.stream().filter(r -> ((Integer) r.get("percent")) == 100).count());
      m.put("certificateCount", certificates.findByStudentIdOrderByIssuedAtDesc(a.getId()).size());
      m.put("attemptCount", attempts.findByStudentIdOrderByCreatedAtDesc(a.getId()).size());
      m.put("totalStars", attempts.findByStudentIdOrderByCreatedAtDesc(a.getId()).stream()
          .mapToInt(ExamAttempt::getStars).sum());
      m.put("courses", catalog("", "").stream().limit(3).toList());
    }
    return m;
  }

  @Transactional
  public Account saveAccount(Long id, AccountForm f) {
    Account actor = admin();
    Account a = id == null ? new Account() : account(id);
    String email = text(f.getEmail(), 180, "El correo").toLowerCase(Locale.ROOT);
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
      throw new BusinessException("El correo no es válido.");
    if (accounts
        .findByEmailIgnoreCase(email)
        .filter(x -> !Objects.equals(x.getId(), id))
        .isPresent()) throw new BusinessException("El correo ya está registrado.");
    if (id != null
        && a.getRole() == Role.ADMIN
        && (!f.isActive() || f.getRole() != Role.ADMIN)
        && accounts.countByRoleAndActiveTrue(Role.ADMIN) <= 1)
      throw new BusinessException("Debe permanecer al menos un administrador activo.");
    if (f.getRole() == null) throw new BusinessException("Selecciona un rol.");
    if (!Objects.toString(f.getPassword(), "").isBlank()) {
      validatePassword(f.getPassword());
      a.setPassword(encoder.encode(f.getPassword()));
    } else if (id == null) throw new BusinessException("La contraseña es obligatoria.");
    // Una identidad con operaciones académicas no puede cambiar de rol.
    if (id != null
        && a.getRole() != f.getRole()
        && (enrollments.findByStudentIdOrderByIdDesc(id).size() > 0
            || courses.assigned(id).size() > 0))
      throw new BusinessException("El usuario tiene vínculos académicos; no puede cambiar de rol.");
    a.setFullName(text(f.getFullName(), 120, "El nombre"));
    a.setEmail(email);
    a.setRole(f.getRole());
    a.setActive(f.isActive());
    a.setProfessionalTitle(optional(f.getProfessionalTitle(), 180));
    a.setPhone(optional(f.getPhone(), 30));
    accounts.save(a);
    audit(actor, id == null ? "CREAR_USUARIO" : "EDITAR_USUARIO", email);
    return a;
  }

  private void validatePassword(String p) {
    if (p == null
        || p.length() < 10
        || p.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
        || !p.matches(".*[A-Z].*")
        || !p.matches(".*[a-z].*")
        || !p.matches(".*[0-9].*"))
      throw new BusinessException(
          "La contraseña requiere al menos 10 caracteres, hasta 72 bytes, mayúsculas, minúsculas y números.");
  }

  @Transactional
  public void register(AccountForm f) {
    String email = text(f.getEmail(), 180, "El correo").toLowerCase(Locale.ROOT);
    validatePassword(f.getPassword());
    if (accounts.findByEmailIgnoreCase(email).isPresent())
      throw new BusinessException("El correo ya está registrado.");
    Account a = new Account();
    a.setFullName(text(f.getFullName(), 120, "El nombre"));
    a.setEmail(email);
    a.setPassword(encoder.encode(f.getPassword()));
    a.setRole(Role.ESTUDIANTE);
    a.setActive(true);
    a.setPhone(optional(f.getPhone(), 30));
    accounts.save(a);
  }

  @Transactional
  public void profile(
      String name, String phone, String title, String oldPassword, String newPassword) {
    Account a = required();
    a.setFullName(text(name, 120, "El nombre"));
    a.setPhone(optional(phone, 30));
    a.setProfessionalTitle(optional(title, 180));
    if (!Objects.toString(newPassword, "").isBlank()) {
      if (!encoder.matches(Objects.toString(oldPassword, ""), a.getPassword()))
        throw new BusinessException("La contraseña actual no coincide.");
      validatePassword(newPassword);
      a.setPassword(encoder.encode(newPassword));
    }
    audit(a, "ACTUALIZAR_PERFIL", "Actualización de datos personales");
  }

  @Transactional
  public Course saveCourse(Long id, CourseForm f) {
    Account actor = admin();
    Course c = id == null ? new Course() : course(id);
    c.setTitle(text(f.getTitle(), 180, "El título"));
    c.setDescription(text(f.getDescription(), 6000, "La descripción"));
    c.setObjectives(text(f.getObjectives(), 2000, "Los objetivos"));
    c.setCategory(text(f.getCategory(), 100, "La categoría"));
    c.setLevel(text(f.getLevel(), 50, "El nivel"));
    if (f.getPrice() == null
        || f.getPrice().signum() < 0
        || f.getPrice().compareTo(new BigDecimal("999999.99")) > 0
        || f.getDurationHours() < 1
        || f.getDurationHours() > 2000)
      throw new BusinessException("El precio o la duración no es válido.");
    c.setPrice(f.getPrice());
    c.setDurationHours(f.getDurationHours());
    c.setPublished(f.isPublished());
    c.setCertificateEnabled(f.isCertificateEnabled());
    Set<Account> teachers = new LinkedHashSet<>();
    for (Long tid : f.getTeacherIds()) {
      Account t = account(tid);
      if (t.getRole() != Role.DOCENTE || !t.isActive())
        throw new BusinessException("Selecciona docentes activos.");
      teachers.add(t);
    }
    c.setTeachers(teachers);
    courses.save(c);
    audit(actor, id == null ? "CREAR_CURSO" : "EDITAR_CURSO", c.getTitle());
    return c;
  }

  @Transactional
  public void deleteCourse(Long id) {
    Account actor = admin();
    Course c = course(id);
    if (enrollments.existsByCourseId(id)
        || payments.existsByCourseId(id)
        || !modules(id).isEmpty()
        || !reviews.findByCourseIdOrderByCreatedAtDesc(id).isEmpty()
        || carts.findAll().stream().anyMatch(i -> i.getCourse().getId().equals(id)))
      throw new BusinessException(
          "El curso tiene datos asociados. Puedes ocultarlo del catálogo desactivando su publicación.");
    courses.delete(c);
    audit(actor, "ELIMINAR_CURSO", c.getTitle());
  }

  @Transactional
  public LearningModule saveModule(Long courseId, Long id, String title, int position) {
    Course c = managed(courseId);
    if (position < 1 || position > 999)
      throw new BusinessException("El orden debe estar entre 1 y 999.");
    LearningModule m =
        id == null
            ? new LearningModule()
            : modules.findById(id).orElseThrow(() -> new BusinessException("El módulo no existe."));
    if (id != null && !m.getCourse().getId().equals(courseId))
      throw new AccessDeniedException("El módulo pertenece a otro curso.");
    m.setCourse(c);
    m.setTitle(text(title, 180, "El nombre del módulo"));
    m.setPosition(position);
    return modules.save(m);
  }

  @Transactional
  public void deleteModule(Long id) {
    var m = modules.findById(id).orElseThrow(() -> new BusinessException("El módulo no existe."));
    managed(m.getCourse().getId());
    if (!moduleContents(id).isEmpty())
      throw new BusinessException("El módulo contiene lecciones y no puede eliminarse.");
    modules.delete(m);
  }

  @Transactional
  public Content saveContent(Long moduleId, Long id, ContentForm f) {
    LearningModule m =
        modules.findById(moduleId).orElseThrow(() -> new BusinessException("El módulo no existe."));
    managed(m.getCourse().getId());
    Content c = id == null ? new Content() : contents.lockById(id).orElseThrow();
    if (id != null && !c.getModule().getId().equals(moduleId))
      throw new AccessDeniedException("El contenido pertenece a otro módulo.");
    if (f.getType() == null
        || (f.getType() == ContentType.EVALUACION
            ? f.getMaxAttempts() < 0 || f.getMaxAttempts() > 20
            : f.getMaxAttempts() < 1 || f.getMaxAttempts() > 10)
        || f.getPosition() < 1
        || f.getPosition() > 999
        || f.getPassPercent() < 0
        || f.getPassPercent() > 100)
      throw new BusinessException("La configuración del contenido no es válida.");
    if (id != null
        && c.getType() != f.getType()
        && (progress.existsByContentId(id)
            || submissions.existsByContentId(id)
            || attempts.existsByContentId(id)
            || discussions.existsByContentId(id)
            || !questions.findByContentIdOrderByPositionAscIdAsc(id).isEmpty()))
      throw new BusinessException(
          "El contenido tiene actividad registrada y no puede cambiar de tipo.");
    if (id != null && attempts.existsByContentId(id) && (c.getPassPercent() != f.getPassPercent()))
      throw new BusinessException(
          "La nota de aprobación no puede cambiar después de una evaluación.");
    String url = optional(f.getResourceUrl(), 1000);
    if (!url.isBlank()) {
      try {
        URI u = URI.create(url);
        if (!"https".equals(u.getScheme()) || u.getHost() == null || u.getUserInfo() != null)
          throw new IllegalArgumentException();
      } catch (IllegalArgumentException ex) {
        throw new BusinessException("El recurso externo debe ser una dirección HTTPS válida.");
      }
    }
    c.setModule(m);
    c.setExamActive(f.isExamActive());
    c.setTitle(text(f.getTitle(), 180, "El título"));
    c.setType(f.getType());
    c.setBody(optional(f.getBody(), 12000));
    c.setResourceUrl(url);
    c.setPreview(f.isPreview());
    c.setPosition(f.getPosition());
    c.setMaxAttempts(f.getMaxAttempts());
    c.setPassPercent(f.getPassPercent());
    return contents.save(c);
  }

  @Transactional
  public void attachMaterial(Long id, String key, String name) {
    Content c = content(id);
    managed(c.getModule().getCourse().getId());
    c.setFileKey(key);
    c.setFileName(name);
  }

  @Transactional
  public void deleteContent(Long id) {
    Content c = content(id);
    managed(c.getModule().getCourse().getId());
    if (progress.existsByContentId(id)
        || submissions.existsByContentId(id)
        || attempts.existsByContentId(id)
        || discussions.existsByContentId(id))
      throw new BusinessException("El contenido tiene historial académico y no puede eliminarse.");
    questions.deleteByContentId(id);
    contents.delete(c);
  }

  public List<CartItem> cart() {
    return carts.findByStudentIdOrderByIdDesc(student().getId());
  }

  public List<Discussion> myComments() {
    return discussions.findByAuthorIdOrderByCreatedAtDesc(student().getId()).stream()
        .filter(d -> !d.isDeleted() && d.getContent().getType() != ContentType.FORO).toList();
  }

  public List<Discussion> contentReviews() {
    admin();
    return discussions.findAllByOrderByCreatedAtDesc().stream()
        .filter(d -> !d.isDeleted() && d.getParent() == null && d.getContent().getType() != ContentType.FORO).toList();
  }

  public Map<String, Object> previewAccount(Long id) {
    admin();
    Account selected = account(id);
    if (selected.getRole() == Role.ADMIN) throw new BusinessException("Selecciona un estudiante o profesor.");
    return Map.of("selected", selected,
        "previewCourses", selected.getRole() == Role.DOCENTE ? courses.assigned(id)
          : enrollments.findByStudentIdOrderByIdDesc(id).stream().filter(Enrollment::isCurrent).map(Enrollment::getCourse).toList());
  }

  @Transactional
  public void addCart(Long courseId) {
    Account a = student();
    accounts.lockById(a.getId());
    Course c = course(courseId);
    if (!c.isPublished()) throw new BusinessException("El curso no está publicado.");
    if (hasAccess(c, a)) throw new BusinessException("Ya tienes acceso a este curso.");
    if (carts.findByStudentIdAndCourseId(a.getId(), courseId).isEmpty()) {
      CartItem item = new CartItem();
      item.setStudent(a);
      item.setCourse(c);
      carts.save(item);
    }
  }

  @Transactional
  public void removeCart(Long courseId) {
    carts.deleteByStudentIdAndCourseId(student().getId(), courseId);
  }

  @Transactional
  public void checkout() {
    Account a = student();
    accounts.lockById(a.getId());
    var items = carts.findByStudentIdOrderByIdDesc(a.getId());
    if (items.isEmpty()) throw new BusinessException("El carrito está vacío.");
    for (CartItem item : items) {
      Course c = item.getCourse();
      if (!c.isPublished() || hasAccess(c, a))
        throw new BusinessException("Revisa la disponibilidad de los cursos del carrito.");
      if (!payments.existsByStudentIdAndCourseIdAndStatus(a.getId(), c.getId(), "PENDIENTE"))
        newPayment(a, c, c.getPrice(), "Solicitud desde el catálogo");
    }
    carts.deleteAll(items);
    audit(
        a,
        "SOLICITAR_MATRICULA",
        "Solicitud de " + items.size() + " curso(s); validación administrativa pendiente");
  }

  private Payment newPayment(Account a, Course c, BigDecimal amount, String reference) {
    Payment p = new Payment();
    p.setStudent(a);
    p.setCourse(c);
    p.setAmount(amount);
    p.setReference(text(reference, 120, "La referencia"));
    p.setStatus("PENDIENTE");
    p.setCreatedAt(LocalDateTime.now());
    return payments.save(p);
  }

  @Transactional
  public Payment createPayment(Long studentId, Long courseId, BigDecimal amount, String reference) {
    admin();
    Account a = account(studentId);
    accounts.lockById(studentId);
    if (a.getRole() != Role.ESTUDIANTE || !a.isActive())
      throw new BusinessException("Selecciona un estudiante activo.");
    if (amount == null || amount.signum() < 0 || amount.compareTo(new BigDecimal("999999.99")) > 0)
      throw new BusinessException("El importe no es válido.");
    if (payments.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, "PENDIENTE"))
      throw new BusinessException("Ya existe una solicitud pendiente para este curso.");
    return newPayment(a, course(courseId), amount, reference);
  }

  @Transactional
  public Enrollment confirmPayment(Long id, LocalDate expiration) {
    Account actor = admin();
    Payment p =
        payments.lockById(id).orElseThrow(() -> new BusinessException("El pago no existe."));
    if (!"PENDIENTE".equals(p.getStatus()))
      throw new BusinessException("Solo pueden confirmarse pagos pendientes.");
    if (expiration == null || !expiration.isAfter(LocalDate.now()))
      throw new BusinessException("La fecha de acceso debe ser posterior a hoy.");
    Account a = accounts.lockById(p.getStudent().getId()).orElseThrow();
    if (!a.isActive() || a.getRole() != Role.ESTUDIANTE)
      throw new BusinessException("La cuenta del estudiante no está activa.");
    Enrollment e =
        enrollments
            .findByStudentIdAndCourseId(a.getId(), p.getCourse().getId())
            .orElseGet(Enrollment::new);
    e.setStudent(a);
    e.setCourse(p.getCourse());
    e.setStatus("ACTIVA");
    e.setStartsAt(LocalDateTime.now());
    e.setExpiresAt(expiration.atTime(23, 59, 59));
    enrollments.save(e);
    p.setStatus("CONFIRMADO");
    p.setConfirmedAt(LocalDateTime.now());
    p.setExpiresAt(e.getExpiresAt());
    carts.deleteByStudentIdAndCourseId(a.getId(), p.getCourse().getId());
    audit(actor, "CONFIRMAR_PAGO", p.getReference() + " / " + p.getCourse().getTitle());
    return e;
  }

  @Transactional
  public void cancelPayment(Long id) {
    Account a = admin();
    Payment p = payments.lockById(id).orElseThrow();
    if (!"PENDIENTE".equals(p.getStatus()))
      throw new BusinessException("Solo puede anularse una solicitud pendiente.");
    p.setStatus("ANULADO");
    audit(a, "ANULAR_PAGO", p.getReference());
  }

  public Payment payment(Long id) {
    admin();
    return payments.findById(id).orElseThrow(() -> new BusinessException("El pago no existe."));
  }

  @Transactional
  public Payment savePayment(Long id, PaymentForm f) {
    Account actor = admin();
    if (f.getAmount() == null || f.getAmount().signum() < 0
        || f.getAmount().compareTo(new BigDecimal("999999.99")) > 0 || f.getAmount().scale() > 2
        || !List.of("CONFIRMADO", "PENDIENTE", "ANULADO").contains(f.getStatus())
        || f.getExpiration() == null) throw new BusinessException("Revisa los datos del pago.");
    Payment p;
    if (id == null) {
      if (!f.getExpiration().isAfter(LocalDate.now()))
        throw new BusinessException("La vigencia debe terminar después de hoy.");
      p = createPayment(f.getStudentId(), f.getCourseId(), f.getAmount(), f.getReference());
    } else {
      p = payments.lockById(id).orElseThrow(() -> new BusinessException("El pago no existe."));
      if (!Objects.equals(p.getVersion(), f.getVersion()))
        throw new BusinessException("El pago cambió mientras lo editabas. Vuelve a abrirlo.");
      if (!p.getStudent().getId().equals(f.getStudentId()) || !p.getCourse().getId().equals(f.getCourseId()))
        throw new BusinessException("El estudiante y el curso de un pago registrado no pueden sustituirse.");
      if ("CONFIRMADO".equals(p.getStatus()) && !"CONFIRMADO".equals(f.getStatus()))
        throw new BusinessException("Un pago confirmado no puede cambiar de estado. Su historial se conserva.");
      if ("ANULADO".equals(p.getStatus()) && !"ANULADO".equals(f.getStatus()))
        throw new BusinessException("Un pago anulado no puede reactivarse. Registra un nuevo pago.");
      p.setAmount(f.getAmount());
      p.setReference(text(f.getReference(), 120, "La referencia"));
    }
    if ("PENDIENTE".equals(p.getStatus()) && "CONFIRMADO".equals(f.getStatus())) {
      confirmPayment(p.getId(), f.getExpiration());
    } else if ("PENDIENTE".equals(p.getStatus()) && "ANULADO".equals(f.getStatus())) {
      cancelPayment(p.getId());
      p.setExpiresAt(f.getExpiration().atTime(23,59,59));
    } else if ("CONFIRMADO".equals(p.getStatus())) {
      var enrollment = enrollments.lockForStudent(p.getStudent().getId(), p.getCourse().getId())
          .orElseThrow(() -> new BusinessException("El pago confirmado no tiene una matrícula asociada."));
      var date = f.getExpiration().atTime(23,59,59);
      if (!date.isAfter(enrollment.getStartsAt()))
        throw new BusinessException("El fin de acceso debe ser posterior al inicio de la matrícula.");
      var oldDate = p.getExpiresAt() == null ? enrollment.getExpiresAt() : p.getExpiresAt();
      if (!date.equals(oldDate)) {
        if (!date.toLocalDate().isAfter(LocalDate.now()))
          throw new BusinessException("Una nueva fecha de vigencia debe ser posterior a hoy.");
        enrollment.setExpiresAt(date);
      }
      p.setExpiresAt(date);
    } else {
      p.setExpiresAt(f.getExpiration().atTime(23,59,59));
    }
    audit(actor, id == null ? "REGISTRAR_PAGO" : "EDITAR_PAGO", p.getId() + " / " + p.getReference());
    return p;
  }

  @Transactional
  public void revokeEnrollment(Long id) {
    Account a = admin();
    Enrollment e = enrollments.findById(id).orElseThrow();
    lockedEnrollment(e);
    e.setStatus("CANCELADA");
    audit(a, "CANCELAR_MATRICULA", e.getStudent().getEmail() + " / " + e.getCourse().getTitle());
  }

  private void lockedEnrollment(Enrollment e) {
    enrollments.lockForStudent(e.getStudent().getId(), e.getCourse().getId());
  }

  @Transactional
  public void complete(Long id) {
    Account a = student();
    Content c = content(id);
    lockedAccess(a, c.getModule().getCourse());
    if (!progress.existsByStudentIdAndContentId(a.getId(), id)) {
      Progress p = new Progress();
      p.setStudent(a);
      p.setContent(c);
      p.setCompletedAt(LocalDateTime.now());
      progress.save(p);
    }
  }

  public List<Submission> contentSubmissions(Long id) {
    Content c = readable(id);
    Account a = required();
    return canManage(c.getModule().getCourse(), a)
        ? submissions.findByContentIdOrderByCreatedAtDesc(id)
        : submissions.findByStudentIdAndContentIdOrderByAttemptNumberDesc(a.getId(), id);
  }

  public Submission readableSubmission(Long id) {
    Submission s =
        submissions.findById(id).orElseThrow(() -> new BusinessException("La entrega no existe."));
    Account a = required();
    if (!a.getId().equals(s.getStudent().getId())
        && !canManage(s.getContent().getModule().getCourse(), a))
      throw new AccessDeniedException("La entrega pertenece a otro estudiante.");
    return s;
  }

  @Transactional
  public Submission submit(Long contentId, String fileKey, String fileName, String comment) {
    Account a = student();
    Content c = content(contentId);
    lockedAccess(a, c.getModule().getCourse());
    if (c.getType() != ContentType.ACTIVIDAD)
      throw new BusinessException("El contenido no es una actividad.");
    long count = submissions.countByStudentIdAndContentId(a.getId(), contentId);
    if (count >= c.getMaxAttempts())
      throw new BusinessException("Alcanzaste el máximo de entregas.");
    Submission s = new Submission();
    s.setStudent(a);
    s.setContent(c);
    s.setAttemptNumber((int) count + 1);
    s.setFileKey(text(fileKey, 100, "El archivo"));
    s.setFileName(text(fileName, 200, "El nombre del archivo"));
    s.setComment(optional(comment, 1000));
    s.setCreatedAt(LocalDateTime.now());
    return submissions.save(s);
  }

  @Transactional
  public void grade(Long id, BigDecimal value, String feedback) {
    Submission s =
        submissions.findById(id).orElseThrow(() -> new BusinessException("La entrega no existe."));
    managed(s.getContent().getModule().getCourse().getId());
    if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("20")) > 0)
      throw new BusinessException("La nota debe estar entre 0 y 20.");
    s.setGrade(value);
    s.setFeedback(optional(feedback, 1000));
    audit(required(), "CALIFICAR_ACTIVIDAD", "Entrega " + id + " / nota " + value);
  }

  public List<Submission> courseSubmissions(Long courseId) {
    managed(courseId);
    return submissions.inCourse(courseId);
  }

  public List<Question> examQuestions(Long id) {
    Content c = readable(id);
    if (c.getType() != ContentType.EVALUACION)
      throw new BusinessException("El contenido no es una evaluación.");
    return questions.findByContentIdOrderByPositionAscIdAsc(id);
  }

  public List<ExamAttempt> examAttempts(Long id) {
    Content c = readable(id);
    Account a = required();
    return canManage(c.getModule().getCourse(), a)
        ? attempts.findByContentIdOrderByCreatedAtDesc(id)
        : attempts.findByStudentIdAndContentIdOrderByAttemptNumberDesc(a.getId(), id);
  }

  public List<ExamAttempt> courseAttempts(Long courseId) {
    managed(courseId);
    return attempts.inCourse(courseId);
  }

  public List<ExamAnswer> result(Long id) {
    ExamAttempt a =
        attempts.findById(id).orElseThrow(() -> new BusinessException("El intento no existe."));
    Account user = required();
    if (!a.getStudent().getId().equals(user.getId())
        && !canManage(a.getContent().getModule().getCourse(), user))
      throw new AccessDeniedException("El resultado pertenece a otro estudiante.");
    return answers.findByAttemptIdOrderByQuestionPositionAscQuestionIdAsc(id);
  }

  public ExamAttempt attempt(Long id) {
    result(id);
    return attempts.findById(id).orElseThrow();
  }

  @Transactional
  public Question saveQuestion(Long contentId, Long id, QuestionForm f) {
    Content c = contents.lockById(contentId).orElseThrow();
    managed(c.getModule().getCourse().getId());
    if (c.getType() != ContentType.EVALUACION)
      throw new BusinessException("Selecciona una evaluación.");
    if (attempts.existsByContentId(contentId))
      throw new BusinessException(
          "La evaluación tiene intentos registrados; su cuestionario no puede modificarse.");
    Question q = id == null ? new Question() : questions.findById(id).orElseThrow();
    if (id != null && !q.getContent().getId().equals(contentId))
      throw new AccessDeniedException("La pregunta pertenece a otra evaluación.");
    if (f.getType() == null
        || f.getPoints() < 1
        || f.getPoints() > 100
        || f.getPosition() < 1
        || f.getPosition() > 999) throw new BusinessException("La pregunta no es válida.");
    String options = optional(f.getOptionsText(), 3000),
        correct = text(f.getCorrectAnswer(), 2000, "La respuesta correcta");
    List<String> values =
        options.isBlank()
            ? List.of()
            : Arrays.stream(options.split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    String[] keys = correct.split("\\|", -1);
    if (f.getType() == QuestionType.OPCION_MULTIPLE
        && (values.size() < 2 || keys.length != 1 || !values.contains(correct)))
      throw new BusinessException("La respuesta correcta debe coincidir con una de las opciones.");
    if (f.getType() == QuestionType.VERDADERO_FALSO
        && !Set.of("Verdadero", "Falso").contains(correct))
      throw new BusinessException("La respuesta debe ser Verdadero o Falso.");
    if (f.getType() == QuestionType.RELACIONAR) {
      if (values.size() < 2
          || values.stream().anyMatch(s -> !s.contains("=>"))
          || keys.length != values.size())
        throw new BusinessException(
            "Define pares izquierda => derecha y una respuesta por par separada con |.");
      List<String> right = values.stream().map(s -> s.split("=>", 2)[1].trim()).toList();
      for (String key : keys)
        if (!right.contains(key))
          throw new BusinessException("La respuesta debe pertenecer a los términos de la derecha.");
    }
    if (f.getType() == QuestionType.SELECCIONAR_ESPACIOS
        && (values.size() < 2 || Arrays.stream(keys).anyMatch(k -> !values.contains(k))))
      throw new BusinessException(
          "Las respuestas de cada espacio deben pertenecer a las opciones.");
    q.setContent(c);
    q.setType(f.getType());
    q.setPrompt(text(f.getPrompt(), 2000, "La pregunta"));
    q.setOptionsText(String.join("\n", values));
    q.setCorrectAnswer(correct);
    q.setExplanation(optional(f.getExplanation(), 2000));
    q.setPoints(f.getPoints());
    q.setPosition(f.getPosition());
    return questions.save(q);
  }

  @Transactional
  public void deleteQuestion(Long id) {
    Question q = questions.findById(id).orElseThrow();
    contents.lockById(q.getContent().getId());
    managed(q.getContent().getModule().getCourse().getId());
    if (attempts.existsByContentId(q.getContent().getId()))
      throw new BusinessException("No puede eliminarse una pregunta con intentos registrados.");
    questions.delete(q);
  }

  @Transactional
  public ExamAttempt evaluate(Long contentId, MultiValueMap<String, String> form) {
    Account a = student();
    Content c = content(contentId);
    lockedAccess(a, c.getModule().getCourse());
    contents.lockById(contentId);
    if (c.getType() != ContentType.EVALUACION)
      throw new BusinessException("El contenido no es una evaluación.");
    if (!c.isExamActive()) throw new BusinessException("Esta evaluación no está activa.");
    long count = attempts.countByStudentIdAndContentId(a.getId(), contentId);
    if (c.getMaxAttempts() > 0 && count >= c.getMaxAttempts())
      throw new BusinessException("Alcanzaste el máximo de intentos.");
    var qs = questions.findByContentIdOrderByPositionAscIdAsc(contentId);
    if (qs.isEmpty()) throw new BusinessException("La evaluación aún no tiene preguntas.");
    Set<String> allowed =
        qs.stream().map(q -> "q_" + q.getId()).collect(java.util.stream.Collectors.toSet());
    for (String key : form.keySet())
      if (key.startsWith("q_") && !allowed.contains(key))
        throw new BusinessException("La evaluación contiene una pregunta no válida.");
    int total = qs.stream().mapToInt(Question::getPoints).sum(), earned = 0;
    List<ExamAnswer> detail = new ArrayList<>();
    for (Question q : qs) {
      List<String> reply = form.getOrDefault("q_" + q.getId(), List.of());
      String answer = optional(String.join(" | ", reply), 3000);
      boolean ok = grader.correct(q, reply);
      ExamAnswer ea = new ExamAnswer();
      ea.setQuestion(q);
      ea.setAnswerText(answer);
      ea.setCorrect(ok);
      ea.setEarnedPoints(ok ? q.getPoints() : 0);
      earned += ea.getEarnedPoints();
      detail.add(ea);
    }
    BigDecimal score = BigDecimal.valueOf(earned * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    ExamAttempt at = new ExamAttempt();
    at.setStudent(a);
    at.setContent(c);
    at.setAttemptNumber((int) count + 1);
    at.setPercent(score);
    at.setPassed(score.compareTo(BigDecimal.valueOf(c.getPassPercent())) >= 0);
    at.setStars(grader.stars(score.doubleValue()));
    at.setCreatedAt(LocalDateTime.now());
    attempts.save(at);
    for (ExamAnswer ea : detail) {
      ea.setAttempt(at);
      answers.save(ea);
    }
    return at;
  }

  public List<Map<String, Object>> posts(Long contentId) {
    readable(contentId);
    return discussions.findByContentIdOrderByCreatedAtAsc(contentId).stream()
        .map(
            d -> {
              Map<String, Object> m = new HashMap<>();
              m.put("post", d);
              m.put("reactionCount", reactions.countByPostId(d.getId()));
              Double avg = ratings.average(d.getId());
              m.put("rating", avg == null ? "—" : String.format(Locale.ROOT, "%.1f", avg));
              return m;
            })
        .toList();
  }

  @Transactional
  public void post(Long contentId, Long parentId, String body) {
    Content c = content(contentId);
    requireAccess(c.getModule().getCourse());
    Discussion d = new Discussion();
    d.setContent(c);
    d.setAuthor(required());
    d.setBody(text(body, 4000, "El comentario"));
    d.setCreatedAt(LocalDateTime.now());
    if (parentId != null) {
      Discussion parent = discussions.findById(parentId).orElseThrow();
      if (!parent.getContent().getId().equals(contentId) || parent.isDeleted())
        throw new BusinessException(
            "La respuesta no pertenece a una publicación disponible en este contenido.");
      d.setParent(parent);
    }
    discussions.save(d);
  }

  private Discussion accessiblePost(Long id) {
    Discussion d = discussions.findById(id).orElseThrow();
    requireAccess(d.getContent().getModule().getCourse());
    return d;
  }

  @Transactional
  public void editPost(Long id, String body) {
    Discussion d = accessiblePost(id);
    if (d.isDeleted() || !d.getAuthor().getId().equals(required().getId()))
      throw new AccessDeniedException("Solo puedes editar tus publicaciones activas.");
    d.setBody(text(body, 4000, "El comentario"));
  }

  @Transactional
  public void deletePost(Long id) {
    Discussion d = accessiblePost(id);
    Account a = required();
    if (!d.getAuthor().getId().equals(a.getId())
        && !canManage(d.getContent().getModule().getCourse(), a))
      throw new AccessDeniedException("No puedes eliminar esta publicación.");
    d.setDeleted(true);
    d.setBody("Publicación retirada.");
  }

  @Transactional
  public void react(Long id, String type) {
    Discussion d = accessiblePost(id);
    if (d.isDeleted()) throw new BusinessException("La publicación fue retirada.");
    if (!Set.of("UTIL", "ME_GUSTA", "INTERESANTE").contains(type))
      throw new BusinessException("La reacción no es válida.");
    Account a = required();
    accounts.lockById(a.getId());
    Reaction r = reactions.findByPostIdAndActorId(id, a.getId()).orElseGet(Reaction::new);
    if (r.getId() != null && r.getType().equals(type)) {
      reactions.delete(r);
      return;
    }
    r.setActor(a);
    r.setPost(d);
    r.setType(type);
    reactions.save(r);
  }

  @Transactional
  public void rate(Long id, int score) {
    Discussion d = accessiblePost(id);
    Account a = required();
    accounts.lockById(a.getId());
    if (d.isDeleted() || d.getAuthor().getId().equals(a.getId()))
      throw new BusinessException("No puedes valorar tu propia publicación ni una retirada.");
    if (score < 1 || score > 5)
      throw new BusinessException("La valoración debe estar entre 1 y 5.");
    Rating r = ratings.findByPostIdAndActorId(id, a.getId()).orElseGet(Rating::new);
    r.setPost(d);
    r.setActor(a);
    r.setScore(score);
    ratings.save(r);
  }

  public List<CourseReview> courseReviews(Long id) {
    return reviews.findByCourseIdOrderByCreatedAtDesc(id);
  }

  @Transactional
  public void review(Long courseId, int score, String body) {
    Account a = student();
    Course c = course(courseId);
    lockedAccess(a, c);
    if (score < 1 || score > 5)
      throw new BusinessException("La valoración debe estar entre 1 y 5.");
    CourseReview r =
        reviews.findByCourseIdAndAuthorId(courseId, a.getId()).orElseGet(CourseReview::new);
    r.setCourse(c);
    r.setAuthor(a);
    r.setBody(text(body, 2000, "La reseña"));
    r.setScore(score);
    r.setCreatedAt(LocalDateTime.now());
    reviews.save(r);
  }

  @Transactional
  public void deleteReview(Long id) {
    CourseReview r = reviews.findById(id).orElseThrow();
    Account a = required();
    if (!a.getId().equals(r.getAuthor().getId()) && !canManage(r.getCourse(), a))
      throw new AccessDeniedException("No puedes retirar esta reseña.");
    reviews.delete(r);
  }

  public List<Certificate> myCertificates() {
    return certificates.findByStudentIdOrderByIssuedAtDesc(student().getId());
  }

  public List<Certificate> allCertificates() {
    admin();
    return certificates.findAllByOrderByIssuedAtDesc();
  }

  public Certificate certificate(Long id) {
    Certificate c =
        certificates
            .findById(id)
            .orElseThrow(() -> new BusinessException("El certificado no existe."));
    Account a = required();
    if (a.getRole() != Role.ADMIN && !c.getStudent().getId().equals(a.getId()))
      throw new AccessDeniedException("El certificado pertenece a otro estudiante.");
    return c;
  }

  public Optional<Certificate> verify(String code) {
    return code != null && code.matches("[0-9]{7}")
        ? certificates.findByCode(code)
        : Optional.empty();
  }

  @Transactional
  public Certificate issue(Long courseId) {
    Account a = student();
    Course c = course(courseId);
    lockedAccess(a, c);
    if (!c.isCertificateEnabled())
      throw new BusinessException("El curso no habilita certificados.");
    if (percent(a.getId(), courseId) != 100)
      throw new BusinessException("Completa el 100 % del curso para obtener tu certificado.");
    var existing = certificates.findByStudentIdAndCourseId(a.getId(), courseId);
    if (existing.isPresent()) return existing.get();
    String code;
    do {
      code =
          String.valueOf(
              java.util.concurrent.ThreadLocalRandom.current().nextInt(1000000, 10000000));
    } while (certificates.existsByCode(code));
    Certificate cert = new Certificate();
    cert.setStudent(a);
    cert.setCourse(c);
    cert.setCode(code);
    cert.setStudentName(a.getFullName());
    cert.setCourseTitle(c.getTitle());
    cert.setIssuedAt(LocalDateTime.now());
    certificates.save(cert);
    audit(a, "EMITIR_CERTIFICADO", code + " / " + c.getTitle());
    return cert;
  }
}
