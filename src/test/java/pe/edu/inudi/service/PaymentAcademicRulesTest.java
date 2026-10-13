package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;
import pe.edu.inudi.web.form.PaymentForm;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:payment-rules;DB_CLOSE_DELAY=-1","app.demo.enabled=true"})
@Transactional
class PaymentAcademicRulesTest {
  @Autowired AcademicService academic;
  @Autowired AccountRepository accounts;
  @Autowired CourseRepository courses;
  @Autowired PaymentRepository payments;
  @Autowired EnrollmentRepository enrollments;
  @Autowired ContentRepository contents;
  @Autowired ProgressRepository progress;
  @Autowired ExamAttemptRepository attempts;

  Account student() { return accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow(); }
  Course course() { return courses.findAll().stream().filter(c->c.getCategory().equals("Investigación")).findFirst().orElseThrow(); }
  Content exam() { return contents.inCourse(course().getId()).stream().filter(c->c.getType()==ContentType.EVALUACION).findFirst().orElseThrow(); }
  PaymentForm form() {
    PaymentForm f=new PaymentForm(); f.setStudentId(student().getId());
    f.setCourseId(courses.findAll().stream().filter(c->c.getCategory().equals("Tecnología")).findFirst().orElseThrow().getId());
    f.setAmount(new BigDecimal("180.00")); f.setReference("Registro manual de prueba");
    f.setExpiration(LocalDate.now().plusMonths(6)); return f;
  }

  @Test @WithMockUser(username="admin@inudi.local",roles="ADMIN")
  void manualConfirmedPaymentCreatesAccessInSameTransaction() {
    var f=form(); var p=academic.savePayment(null,f);
    assertThat(p.getStatus()).isEqualTo("CONFIRMADO");
    assertThat(p.getConfirmedAt()).isNotNull();
    assertThat(p.getExpiresAt().toLocalDate()).isEqualTo(f.getExpiration());
    assertThat(enrollments.findByStudentIdAndCourseId(f.getStudentId(),f.getCourseId()).orElseThrow().isCurrent()).isTrue();
  }

  @Test @WithMockUser(username="admin@inudi.local",roles="ADMIN")
  void manualPaymentEditsMetadataAndValidityWithoutNewEnrollment() {
    var f=form(); var p=academic.savePayment(null,f); payments.flush();
    Long enrollmentId=enrollments.findByStudentIdAndCourseId(f.getStudentId(),f.getCourseId()).orElseThrow().getId();
    f.setVersion(p.getVersion()); f.setAmount(new BigDecimal("165.50")); f.setReference("Referencia corregida");
    f.setExpiration(LocalDate.now().plusMonths(9)); academic.savePayment(p.getId(),f);
    var enrollment=enrollments.findByStudentIdAndCourseId(f.getStudentId(),f.getCourseId()).orElseThrow();
    assertThat(enrollment.getId()).isEqualTo(enrollmentId);
    assertThat(enrollment.getExpiresAt().toLocalDate()).isEqualTo(f.getExpiration());
    assertThat(p.getAmount()).isEqualByComparingTo("165.50");
  }

  @Test @WithMockUser(username="admin@inudi.local",roles="ADMIN")
  void paymentRejectsStaleEditAndReassignment() {
    var f=form(); var p=academic.savePayment(null,f); payments.flush();
    f.setVersion(-1L);
    assertThatThrownBy(()->academic.savePayment(p.getId(),f)).isInstanceOf(BusinessException.class).hasMessageContaining("cambió");
    f.setVersion(p.getVersion()); f.setCourseId(course().getId());
    assertThatThrownBy(()->academic.savePayment(p.getId(),f)).isInstanceOf(BusinessException.class).hasMessageContaining("sustituirse");
  }

  @Test @WithMockUser(username="admin@inudi.local",roles="ADMIN")
  void paymentRejectsNegativeAndExpiredNewRecords() {
    var f=form(); f.setAmount(new BigDecimal("-1"));
    assertThatThrownBy(()->academic.savePayment(null,f)).isInstanceOf(BusinessException.class);
    f.setAmount(BigDecimal.ZERO); f.setExpiration(LocalDate.now().minusDays(1));
    assertThatThrownBy(()->academic.savePayment(null,f)).isInstanceOf(BusinessException.class);
  }

  @Test @WithMockUser(username="admin@inudi.local",roles="ADMIN")
  void pendingManualPaymentCanBeConfirmedFromEditor() {
    var f=form(); f.setStatus("PENDIENTE"); var p=academic.savePayment(null,f); payments.flush();
    assertThat(enrollments.findByStudentIdAndCourseId(f.getStudentId(),f.getCourseId())).isEmpty();
    f.setVersion(p.getVersion()); f.setStatus("CONFIRMADO"); academic.savePayment(p.getId(),f);
    assertThat(enrollments.findByStudentIdAndCourseId(f.getStudentId(),f.getCourseId()).orElseThrow().isCurrent()).isTrue();
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void studentCannotRegisterPayments() {
    assertThatThrownBy(()->academic.savePayment(null,form())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void progressIsIndependentOfSubmissionExamAndDiscussionAndIsIdempotent() {
    for (Content content:contents.inCourse(course().getId())) {
      academic.complete(content.getId()); academic.complete(content.getId());
    }
    assertThat(academic.percent(student().getId(),course().getId())).isEqualTo(100);
    assertThat(progress.completed(student().getId(),course().getId())).isEqualTo(contents.inCourse(course().getId()).size());
    assertThat(academic.issue(course().getId()).getId()).isEqualTo(academic.issue(course().getId()).getId());
  }

  @Test @WithMockUser(username="docente@inudi.local",roles="DOCENTE")
  void teacherCannotTrackStudentProgress() {
    assertThatThrownBy(()->academic.complete(exam().getId())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void nextContentExcludesAlreadyCompletedContents() {
    var row=academic.myCourses().stream().filter(r->((Course)r.get("course")).getId().equals(course().getId())).findFirst().orElseThrow();
    Content next=(Content)row.get("nextContent");
    assertThat(next).isNotNull(); academic.complete(next.getId());
    var after=academic.myCourses().stream().filter(r->((Course)r.get("course")).getId().equals(course().getId())).findFirst().orElseThrow();
    assertThat(((Content)after.get("nextContent")).getId()).isNotEqualTo(next.getId());
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void inactiveExamDoesNotConsumeAttempt() {
    var exam=exam(); exam.setExamActive(false); contents.flush();
    assertThatThrownBy(()->academic.evaluate(exam.getId(),new LinkedMultiValueMap<>())).isInstanceOf(BusinessException.class).hasMessageContaining("no está activa");
    assertThat(attempts.countByStudentIdAndContentId(student().getId(),exam.getId())).isZero();
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void unlimitedExamAcceptsMoreThanTwentyAttemptsAndReturnsSequentialNumbers() {
    var exam=exam(); exam.setMaxAttempts(0); contents.flush();
    for (int n=1;n<=21;n++) assertThat(academic.evaluate(exam.getId(),new LinkedMultiValueMap<>()).getAttemptNumber()).isEqualTo(n);
  }

  @Test @WithMockUser(username="estudiante@inudi.local",roles="ESTUDIANTE")
  void zeroThresholdIsAllowedForConfiguredExam() {
    var exam=exam(); exam.setPassPercent(0); contents.flush();
    assertThat(academic.evaluate(exam.getId(),new LinkedMultiValueMap<>()).isPassed()).isTrue();
  }
}
