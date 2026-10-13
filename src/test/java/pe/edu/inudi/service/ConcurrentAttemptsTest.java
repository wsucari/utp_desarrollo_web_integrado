package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import pe.edu.inudi.domain.ContentType;
import pe.edu.inudi.repository.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:concurrency-tests;DB_CLOSE_DELAY=-1",
      "app.demo.enabled=true"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrentAttemptsTest {
  @Autowired AcademicService academic;
  @Autowired ContentRepository contents;
  @Autowired QuestionRepository questions;
  @Autowired AccountRepository accounts;
  @Autowired ExamAttemptRepository attempts;

  @Test
  void concurrentAttemptsCannotExceedThreeOrDuplicateNumbers() throws Exception {
    var exam =
        contents.findAll().stream()
            .filter(c -> c.getType() == ContentType.EVALUACION)
            .findFirst()
            .orElseThrow();
    var form = new LinkedMultiValueMap<String, String>();
    for (var q : questions.findByContentIdOrderByPositionAscIdAsc(exam.getId()))
      form.put("q_" + q.getId(), Arrays.asList(q.getCorrectAnswer().split("\\|")));
    var start = new CountDownLatch(1);
    List<Future<Boolean>> jobs = new ArrayList<>();
    try (var workers = Executors.newFixedThreadPool(6)) {
      for (int i = 0; i < 6; i++)
        jobs.add(
            workers.submit(
                () -> {
                  start.await();
                  var context = SecurityContextHolder.createEmptyContext();
                  context.setAuthentication(
                      new UsernamePasswordAuthenticationToken(
                          "estudiante@inudi.local",
                          "",
                          List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))));
                  SecurityContextHolder.setContext(context);
                  try {
                    academic.evaluate(exam.getId(), form);
                    return true;
                  } catch (BusinessException ex) {
                    assertThat(ex.getMessage()).contains("máximo de intentos");
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      int successful = 0;
      for (var job : jobs) if (job.get(20, TimeUnit.SECONDS)) successful++;
      assertThat(successful).isEqualTo(3);
    }
    var student = accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow();
    assertThat(
            attempts.findByStudentIdAndContentIdOrderByAttemptNumberDesc(
                student.getId(), exam.getId()))
        .extracting(a -> a.getAttemptNumber())
        .containsExactly(3, 2, 1);
  }
}
