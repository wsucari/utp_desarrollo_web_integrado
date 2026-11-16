package pe.edu.inudi.web;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1",
      "app.demo.enabled=true",
      "app.storage=target/api-test-uploads"
    })
@AutoConfigureMockMvc
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired CourseRepository courses;
  @Autowired ContentRepository contents;
  @Autowired AccountRepository accounts;
  @Autowired EnrollmentRepository enrollments;

  @Test
  void catalogUsesJsonWithoutPrivateFields() throws Exception {
    var response =
        mvc.perform(get("/api/cursos"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.courses").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response)
        .doesNotContain("password", "fileKey", "correctAnswer", "professional_title");
    mvc.perform(get("/api/cursos").param("search", "zzzz-no-existe"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courses").isEmpty());
  }

  @Test
  void anonymousApiReturns401AndRoleFailureReturns403() throws Exception {
    mvc.perform(get("/api/panel"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));
    mvc.perform(get("/api/admin/usuarios").with(user("estudiante@inudi.local").roles("ESTUDIANTE")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void csrfIsRequiredForApiWrites() throws Exception {
    mvc.perform(post("/api/carrito/1").with(user("estudiante@inudi.local").roles("ESTUDIANTE")))
        .andExpect(status().isForbidden());
  }

  @Test
  void courseCrudUsesExistingRulesAndServerValidation() throws Exception {
    var admin = user("admin@inudi.local").roles("ADMIN");
    mvc.perform(
            post("/api/admin/cursos")
                .with(admin)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.title").exists());
    var form =
        Map.of(
            "title",
            "Curso API de prueba",
            "description",
            "Descripción verificable",
            "objectives",
            "Comprobar integración",
            "category",
            "Pruebas",
            "level",
            "Inicial",
            "durationHours",
            10,
            "price",
            25,
            "published",
            true,
            "certificateEnabled",
            true);
    var result =
        mvc.perform(
                post("/api/admin/cursos")
                    .with(admin)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(form)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Curso API de prueba"))
            .andReturn();
    long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    assertThat(courses.findById(id)).isPresent();
    mvc.perform(delete("/api/admin/cursos/" + id).with(admin).with(csrf()))
        .andExpect(status().isNoContent());
    assertThat(courses.findById(id)).isEmpty();
  }

  @Test
  void paymentEnablesEnrollmentAndPreservesVersion() throws Exception {
    var admin = user("admin@inudi.local").roles("ADMIN");
    var student = accounts.findByEmailIgnoreCase("camila@inudi.local").orElseThrow();
    var course = courses.findAll().getLast();
    var form =
        Map.of(
            "studentId",
            student.getId(),
            "courseId",
            course.getId(),
            "amount",
            35,
            "reference",
            "API matrícula",
            "status",
            "CONFIRMADO",
            "expiration",
            LocalDate.now().plusMonths(8).toString());
    var result =
        mvc.perform(
                post("/api/admin/pagos")
                    .with(admin)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(form)))
            .andExpect(status().isCreated())
            .andReturn();
    var payment = json.readTree(result.getResponse().getContentAsString());
    assertThat(
            enrollments
                .findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow()
                .isCurrent())
        .isTrue();
    mvc.perform(get("/api/mis-cursos").with(user("camila@inudi.local").roles("ESTUDIANTE")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(course.getTitle())));
    var edit = new java.util.LinkedHashMap<String, Object>(form);
    edit.put("version", payment.get("version").asLong());
    edit.put("reference", "Edición API");
    mvc.perform(
            put("/api/admin/pagos/" + payment.get("id").asLong())
                .with(admin)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(edit)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reference").value("Edición API"));
  }

  @Test
  void studentQuestionsNeverExposeCorrectAnswers() throws Exception {
    var student = user("estudiante@inudi.local").roles("ESTUDIANTE");
    var course =
        enrollments
            .findByStudentIdOrderByIdDesc(
                accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow().getId())
            .getFirst()
            .getCourse();
    var exam =
        contents.inCourse(course.getId()).stream()
            .filter(c -> c.getType() == ContentType.EVALUACION)
            .findFirst()
            .orElseThrow();
    var response =
        mvc.perform(get("/api/contenidos/" + exam.getId()).with(student))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questions[0].prompt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).doesNotContain("correctAnswer", "explanation", "password", "fileKey");
    mvc.perform(get("/api/gestion/contenidos/" + exam.getId() + "/preguntas").with(student))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/gestion/contenidos/" + exam.getId() + "/preguntas")
                .with(user("docente@inudi.local").roles("DOCENTE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[0].correctAnswer").exists());
  }

  @Test
  void loginEstablishesSessionWithoutJwt() throws Exception {
    var csrfResult = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
    var session =
        (org.springframework.mock.web.MockHttpSession) csrfResult.getRequest().getSession(false);
    var response =
        mvc.perform(
                post("/api/auth/login")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"estudiante@inudi.local\",\"password\":\"Inudi2026!Demo\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("ESTUDIANTE"))
            .andReturn();
    var loggedIn =
        (org.springframework.mock.web.MockHttpSession) response.getRequest().getSession(false);
    mvc.perform(get("/api/auth/session").session(loggedIn))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("estudiante@inudi.local"));
    mvc.perform(post("/api/auth/logout").session(loggedIn).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void badLoginReturnsJson401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"estudiante@inudi.local\",\"password\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void missingResourceAndPublicSyllabusUseSafeContracts() throws Exception {
    mvc.perform(get("/api/cursos/99999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
    var course = courses.findAll().getFirst();
    var response =
        mvc.perform(get("/api/cursos/" + course.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response)
        .doesNotContain("\"body\"", "resourceUrl", "fileName", "password", "correctAnswer");
    var privateContent =
        contents.inCourse(course.getId()).stream()
            .filter(c -> !c.isPreview())
            .findFirst()
            .orElseThrow();
    mvc.perform(get("/api/contenidos/" + privateContent.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void publicRegistrationCannotElevatePrivileges() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "fullName",
                            "Registro API",
                            "email",
                            "registro-api@inudi.local",
                            "password",
                            "Inudi2026!Demo",
                            "role",
                            "ADMIN",
                            "active",
                            false))))
        .andExpect(status().isCreated());
    var account = accounts.findByEmailIgnoreCase("registro-api@inudi.local").orElseThrow();
    assertThat(account.getRole()).isEqualTo(Role.ESTUDIANTE);
    assertThat(account.isActive()).isTrue();
  }

  @Test
  void inactiveAccountIsRejectedBeforeCallingBusinessOperations() throws Exception {
    Account account = new Account();
    account.setFullName("Cuenta inactiva API");
    account.setEmail("inactiva-api@inudi.local");
    account.setPassword("hash-de-prueba");
    account.setRole(Role.ESTUDIANTE);
    account.setActive(false);
    accounts.save(account);
    mvc.perform(get("/api/panel").with(user(account.getEmail()).roles("ESTUDIANTE")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void teacherRequiresAssignmentInAdditionToRole() throws Exception {
    Account account = new Account();
    account.setFullName("Docente sin curso API");
    account.setEmail("docente-sin-curso@inudi.local");
    account.setPassword("hash-de-prueba");
    account.setRole(Role.DOCENTE);
    account.setActive(true);
    accounts.save(account);
    mvc.perform(
            get("/api/gestion/cursos/" + courses.findAll().getFirst().getId() + "/seguimiento")
                .with(user(account.getEmail()).roles("DOCENTE")))
        .andExpect(status().isForbidden());
  }

  @Test
  void profileKeepsTheExistingNameRuleWithoutAddingAMinimum() throws Exception {
    Account account = new Account();
    account.setFullName("Perfil de prueba");
    account.setEmail("perfil-api@inudi.local");
    account.setPassword("hash-de-prueba");
    account.setRole(Role.ESTUDIANTE);
    account.setActive(true);
    accounts.save(account);
    mvc.perform(
            put("/api/perfil")
                .with(user(account.getEmail()).roles("ESTUDIANTE"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"fullName\":\"A\",\"phone\":\"\",\"professionalTitle\":\"\",\"oldPassword\":\"\",\"newPassword\":\"\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("A"));
  }
}
