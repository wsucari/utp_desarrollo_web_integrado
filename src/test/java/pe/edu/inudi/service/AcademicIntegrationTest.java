package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;
import pe.edu.inudi.web.form.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:academic-tests;DB_CLOSE_DELAY=-1",
      "app.demo.enabled=true"
    })
@AutoConfigureMockMvc
@Transactional
class AcademicIntegrationTest {
  @Autowired AcademicService academic;
  @Autowired MockMvc mvc;
  @Autowired AccountRepository accounts;
  @Autowired CourseRepository courses;
  @Autowired ContentRepository contents;
  @Autowired EnrollmentRepository enrollments;
  @Autowired ProgressRepository progress;
  @Autowired SubmissionRepository submissions;
  @Autowired QuestionRepository questions;
  @Autowired ExamAttemptRepository attempts;
  @Autowired CertificateRepository certificates;
  @Autowired DiscussionRepository discussions;
  @Autowired ReactionRepository reactions;
  @Autowired RatingRepository ratings;
  @Autowired PaymentRepository payments;

  private Account student() {
    return accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow();
  }

  private Course course() {
    return courses.findAll().stream()
        .filter(c -> c.getCategory().equals("Investigación"))
        .findFirst()
        .orElseThrow();
  }

  private Content lesson(ContentType type) {
    return contents.inCourse(course().getId()).stream()
        .filter(c -> c.getType() == type)
        .findFirst()
        .orElseThrow();
  }

  private LinkedMultiValueMap<String, String> correctAnswers() {
    var form = new LinkedMultiValueMap<String, String>();
    for (var q :
        questions.findByContentIdOrderByPositionAscIdAsc(lesson(ContentType.EVALUACION).getId()))
      form.put("q_" + q.getId(), Arrays.asList(q.getCorrectAnswer().split("\\|")));
    return form;
  }

  @Test
  void catalogIsPublicAndSearchUsesJpql() throws Exception {
    mvc.perform(get("/").param("q", "metodología"))
        .andExpect(status().isOk())
        .andExpect(view().name("catalog"));
    assertThat(academic.catalog("Metodología", "Investigación")).hasSize(1);
  }

  @Test
  void anonymousPreviewIsAllowedButPrivateLessonIsNot() {
    assertThat(academic.readable(lesson(ContentType.TEXTO).getId())).isNotNull();
    assertThatThrownBy(() -> academic.readable(lesson(ContentType.MATERIAL).getId()))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void privateRoutesRequireLogin() throws Exception {
    mvc.perform(get("/panel")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void csrfRejectsUnprotectedWrites() throws Exception {
    mvc.perform(post("/contenidos/" + lesson(ContentType.VIDEO).getId() + "/completar"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void studentCannotUseAdministration() throws Exception {
    mvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
    assertThatThrownBy(academic::admin).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void progressIsIdempotent() {
    Long id = lesson(ContentType.VIDEO).getId();
    academic.complete(id);
    academic.complete(id);
    assertThat(progress.completed(student().getId(), course().getId())).isEqualTo(2);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void expiredEnrollmentDoesNotGrantAccessOrDeleteHistory() {
    var e =
        enrollments.findByStudentIdAndCourseId(student().getId(), course().getId()).orElseThrow();
    e.setExpiresAt(LocalDateTime.now().minusSeconds(1));
    assertThat(academic.hasAccess(course(), student())).isFalse();
    assertThat(progress.completed(student().getId(), course().getId())).isEqualTo(1);
    assertThatThrownBy(() -> academic.complete(lesson(ContentType.VIDEO).getId()))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void evaluationScoresFiveTypesAndLimitsAttempts() {
    Long id = lesson(ContentType.EVALUACION).getId();
    var result = academic.evaluate(id, correctAnswers());
    assertThat(result.getPercent()).isEqualByComparingTo("100");
    assertThat(result.isPassed()).isTrue();
    assertThat(result.getStars()).isEqualTo(3);
    academic.evaluate(id, correctAnswers());
    academic.evaluate(id, correctAnswers());
    assertThatThrownBy(() -> academic.evaluate(id, correctAnswers()))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("máximo");
    assertThat(attempts.countByStudentIdAndContentId(student().getId(), id)).isEqualTo(3);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void forgedQuestionIdIsRejected() {
    var form = correctAnswers();
    form.add("q_999999", "falsa");
    assertThatThrownBy(() -> academic.evaluate(lesson(ContentType.EVALUACION).getId(), form))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void emptyAnswersFailWithZeroScore() {
    var result =
        academic.evaluate(lesson(ContentType.EVALUACION).getId(), new LinkedMultiValueMap<>());
    assertThat(result.getPercent()).isEqualByComparingTo("0");
    assertThat(result.isPassed()).isFalse();
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void scoreUsesQuestionWeightsInsteadOfQuestionCount() {
    var qs =
        questions.findByContentIdOrderByPositionAscIdAsc(lesson(ContentType.EVALUACION).getId());
    var first = qs.getFirst();
    first.setPoints(8);
    var form = new LinkedMultiValueMap<String, String>();
    form.add("q_" + first.getId(), first.getCorrectAnswer());
    assertThat(academic.evaluate(first.getContent().getId(), form).getPercent())
        .isEqualByComparingTo("50");
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void activityProgressDoesNotRequireSubmission() {
    Long id = lesson(ContentType.ACTIVIDAD).getId();
    academic.complete(id);
    assertThat(progress.existsByStudentIdAndContentId(student().getId(), id)).isTrue();
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void submissionAttemptsAreLimited() {
    Long id = lesson(ContentType.ACTIVIDAD).getId();
    for (int n = 1; n <= 3; n++)
      assertThat(
              academic
                  .submit(
                      id,
                      "00000000-0000-0000-0000-000000000001.txt",
                      "propuesta.txt",
                      "Mi actividad")
                  .getAttemptNumber())
          .isEqualTo(n);
    assertThatThrownBy(
            () -> academic.submit(id, "00000000-0000-0000-0000-000000000001.txt", "x.txt", ""))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "camila@inudi.local", roles = "ESTUDIANTE")
  void anotherStudentCannotReadSubmissionOrExamResult() {
    var s = new Submission();
    s.setStudent(student());
    s.setContent(lesson(ContentType.ACTIVIDAD));
    s.setAttemptNumber(1);
    s.setFileKey("x");
    s.setFileName("x");
    s.setComment("");
    s.setCreatedAt(LocalDateTime.now());
    submissions.save(s);
    assertThatThrownBy(() -> academic.readableSubmission(s.getId()))
        .isInstanceOf(AccessDeniedException.class);
    var at = new ExamAttempt();
    at.setStudent(student());
    at.setContent(lesson(ContentType.EVALUACION));
    at.setAttemptNumber(1);
    at.setPercent(BigDecimal.ZERO);
    at.setCreatedAt(LocalDateTime.now());
    attempts.save(at);
    assertThatThrownBy(() -> academic.result(at.getId())).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "docente@inudi.local", roles = "DOCENTE")
  void teacherCannotManageUnassignedCourse() {
    Course c = new Course();
    c.setTitle("Curso privado");
    c.setCategory("Prueba");
    c.setLevel("Inicial");
    c.setDescription("Descripción");
    c.setObjectives("Objetivo");
    c.setDurationHours(10);
    c.setPrice(BigDecimal.ZERO);
    courses.save(c);
    assertThatThrownBy(() -> academic.managed(c.getId())).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "docente@inudi.local", roles = "DOCENTE")
  void teacherCanGradeAssignedCourseButNotBeyondScale() {
    var s = new Submission();
    s.setStudent(student());
    s.setContent(lesson(ContentType.ACTIVIDAD));
    s.setAttemptNumber(1);
    s.setFileKey("x");
    s.setFileName("x");
    s.setComment("");
    s.setCreatedAt(LocalDateTime.now());
    submissions.save(s);
    academic.grade(s.getId(), new BigDecimal("18.50"), "Objetivos claros");
    assertThat(s.getGrade()).isEqualByComparingTo("18.50");
    assertThatThrownBy(() -> academic.grade(s.getId(), new BigDecimal("21"), ""))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "docente@inudi.local", roles = "DOCENTE")
  void questionsCannotChangeAfterFirstAttempt() {
    var at = new ExamAttempt();
    at.setStudent(student());
    at.setContent(lesson(ContentType.EVALUACION));
    at.setAttemptNumber(1);
    at.setPercent(BigDecimal.ZERO);
    at.setCreatedAt(LocalDateTime.now());
    attempts.saveAndFlush(at);
    QuestionForm f = new QuestionForm();
    f.setPrompt("Nueva pregunta");
    f.setCorrectAnswer("Verdadero");
    f.setType(QuestionType.VERDADERO_FALSO);
    assertThatThrownBy(() -> academic.saveQuestion(at.getContent().getId(), null, f))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void forumRepliesMustRemainInSameContent() {
    var parent =
        discussions.findByContentIdOrderByCreatedAtAsc(lesson(ContentType.FORO).getId()).getFirst();
    assertThatThrownBy(
            () ->
                academic.post(
                    lesson(ContentType.VIDEO).getId(), parent.getId(), "Respuesta cruzada"))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void forumReactionsToggleAndRatingsUpdate() {
    var d =
        discussions.findByContentIdOrderByCreatedAtAsc(lesson(ContentType.FORO).getId()).getFirst();
    academic.react(d.getId(), "UTIL");
    assertThat(reactions.countByPostId(d.getId())).isEqualTo(1);
    academic.react(d.getId(), "UTIL");
    assertThat(reactions.countByPostId(d.getId())).isZero();
    academic.rate(d.getId(), 3);
    academic.rate(d.getId(), 5);
    assertThat(ratings.average(d.getId())).isEqualTo(5.0);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void softDeletedParentPreservesReplies() {
    Long id = lesson(ContentType.FORO).getId();
    academic.post(id, null, "Mi aporte");
    var mine =
        discussions.findByContentIdOrderByCreatedAtAsc(id).stream()
            .filter(d -> d.getAuthor().getId().equals(student().getId()))
            .findFirst()
            .orElseThrow();
    academic.post(id, mine.getId(), "Mi respuesta");
    academic.deletePost(mine.getId());
    assertThat(mine.isDeleted()).isTrue();
    assertThat(discussions.findByContentIdOrderByCreatedAtAsc(id)).hasSize(3);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void selfRatingIsRejected() {
    Long id = lesson(ContentType.FORO).getId();
    academic.post(id, null, "Mi aporte");
    var mine =
        discussions.findByContentIdOrderByCreatedAtAsc(id).stream()
            .filter(d -> d.getAuthor().getId().equals(student().getId()))
            .findFirst()
            .orElseThrow();
    assertThatThrownBy(() -> academic.rate(mine.getId(), 5)).isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void certificatesRequireCompletionAndAreIdempotent() {
    for (Content c : contents.inCourse(course().getId()))
      if (!progress.existsByStudentIdAndContentId(student().getId(), c.getId())) {
        Progress p = new Progress();
        p.setStudent(student());
        p.setContent(c);
        p.setCompletedAt(LocalDateTime.now());
        progress.save(p);
      }
    var first = academic.issue(course().getId());
    var second = academic.issue(course().getId());
    assertThat(first.getId()).isEqualTo(second.getId());
    assertThat(first.getCode()).matches("[0-9]{7}");
    assertThat(academic.verify(first.getCode())).isPresent();
    assertThat(certificates.count()).isEqualTo(1);
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void incompleteCourseCannotIssueCertificate() {
    assertThatThrownBy(() -> academic.issue(course().getId()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void paymentConfirmationAtomicallyCreatesEnrollment() {
    var c =
        courses.findAll().stream()
            .filter(x -> x.getCategory().equals("Tecnología"))
            .findFirst()
            .orElseThrow();
    var p = academic.createPayment(student().getId(), c.getId(), BigDecimal.ZERO, "BECA-PRUEBA");
    assertThat(enrollments.findByStudentIdAndCourseId(student().getId(), c.getId())).isEmpty();
    var e = academic.confirmPayment(p.getId(), LocalDate.now().plusMonths(4));
    assertThat(e.isCurrent()).isTrue();
    assertThat(p.getStatus()).isEqualTo("CONFIRMADO");
    assertThatThrownBy(() -> academic.confirmPayment(p.getId(), LocalDate.now().plusYears(1)))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void invalidExpirationDoesNotConfirmPayment() {
    var c =
        courses.findAll().stream()
            .filter(x -> x.getCategory().equals("Tecnología"))
            .findFirst()
            .orElseThrow();
    var p = academic.createPayment(student().getId(), c.getId(), BigDecimal.ONE, "PRUEBA");
    assertThatThrownBy(() -> academic.confirmPayment(p.getId(), LocalDate.now().minusDays(1)))
        .isInstanceOf(BusinessException.class);
    assertThat(p.getStatus()).isEqualTo("PENDIENTE");
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void lastActiveAdministratorIsProtected() {
    var a = accounts.findByEmailIgnoreCase("admin@inudi.local").orElseThrow();
    AccountForm f = new AccountForm();
    f.setFullName(a.getFullName());
    f.setEmail(a.getEmail());
    f.setRole(Role.ADMIN);
    f.setActive(false);
    assertThatThrownBy(() -> academic.saveAccount(a.getId(), f))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void courseWithHistoryCannotBeDeleted() {
    assertThatThrownBy(() -> academic.deleteCourse(course().getId()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void courseCrudSupportsSafeDeletionOfEmptyCourse() {
    CourseForm f = new CourseForm();
    f.setTitle("Curso de prueba");
    f.setDescription("Descripción de prueba");
    f.setObjectives("Objetivos de prueba");
    f.setCategory("Prueba");
    f.setLevel("Inicial");
    var c = academic.saveCourse(null, f);
    f.setTitle("Curso actualizado");
    academic.saveCourse(c.getId(), f);
    assertThat(academic.course(c.getId()).getTitle()).isEqualTo("Curso actualizado");
    academic.deleteCourse(c.getId());
    assertThat(courses.findById(c.getId())).isEmpty();
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void studentMvcFlowReturnsJspAndRedirectsAfterWrite() throws Exception {
    mvc.perform(get("/mis-cursos")).andExpect(status().isOk()).andExpect(view().name("my-courses"));
    mvc.perform(
            post("/contenidos/" + lesson(ContentType.VIDEO).getId() + "/completar").with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "estudiante@inudi.local", roles = "ESTUDIANTE")
  void inactiveAccountIsLoggedOutImmediately() throws Exception {
    student().setActive(false);
    mvc.perform(get("/panel"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?sesion"));
  }

  @Test
  @WithMockUser(username = "admin@inudi.local", roles = "ADMIN")
  void externalResourceRejectsJavascriptUrls() {
    ContentForm f = new ContentForm();
    f.setTitle("Recurso");
    f.setResourceUrl("javascript:alert(1)");
    assertThatThrownBy(
            () -> academic.saveContent(lesson(ContentType.TEXTO).getModule().getId(), null, f))
        .isInstanceOf(BusinessException.class);
  }
}
