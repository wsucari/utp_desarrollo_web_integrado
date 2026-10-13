package pe.edu.inudi.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;

/** Datos académicos ficticios, exclusivamente para la demostración local. */
@Component
@RequiredArgsConstructor
public class DemoData implements CommandLineRunner {
  private final AccountRepository accounts;
  private final CourseRepository courses;
  private final LearningModuleRepository modules;
  private final ContentRepository contents;
  private final EnrollmentRepository enrollments;
  private final PaymentRepository payments;
  private final QuestionRepository questions;
  private final DiscussionRepository discussions;
  private final ProgressRepository progress;
  private final PasswordEncoder encoder;

  @Value("${app.demo.enabled}")
  private boolean enabled;

  @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
  private String bootstrapEmail;

  @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
  private String bootstrapPassword;

  @Override
  @Transactional
  public void run(String... args) {
    if (accounts.count() > 0) return;
    if (!enabled) {
      if (bootstrapEmail.isBlank()
          || !bootstrapEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
          || bootstrapPassword.length() < 12
          || bootstrapPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new IllegalStateException(
            "Una base vacía sin demostración requiere BOOTSTRAP_ADMIN_EMAIL y BOOTSTRAP_ADMIN_PASSWORD de 12 caracteres o más.");
      Account a = new Account();
      a.setFullName("Administración de Inudi Perú");
      a.setEmail(bootstrapEmail.toLowerCase(Locale.ROOT));
      a.setPassword(encoder.encode(bootstrapPassword));
      a.setRole(Role.ADMIN);
      a.setActive(true);
      accounts.save(a);
      return;
    }
    Account admin = account("Administrador de Inudi", "admin@inudi.local", Role.ADMIN);
    Account teacher = account("Valeria Mendoza", "docente@inudi.local", Role.DOCENTE);
    teacher.setProfessionalTitle("Docente de metodología e investigación");
    Account student = account("Alex Rivera", "estudiante@inudi.local", Role.ESTUDIANTE);
    Account second = account("Camila Torres", "camila@inudi.local", Role.ESTUDIANTE);
    String[] titles = {
      "Metodología de la investigación científica",
      "Redacción y publicación de artículos científicos",
      "Herramientas digitales para la investigación"
    };
    String[] categories = {"Investigación", "Producción científica", "Tecnología"};
    String[] descriptions = {
      "Un recorrido práctico para formular un problema de investigación, construir objetivos coherentes y organizar un diseño metodológico que responda a una pregunta relevante.",
      "Desarrolla una escritura académica clara. Trabaja la estructura de un artículo, la argumentación, las citas y la preparación de un manuscrito para su evaluación editorial.",
      "Organiza fuentes y datos con herramientas digitales. Aprende a construir un flujo de trabajo reproducible, colaborativo y responsable para tu proyecto de investigación."
    };
    for (int i = 0; i < titles.length; i++) {
      Course c = new Course();
      c.setTitle(titles[i]);
      c.setCategory(categories[i]);
      c.setDescription(descriptions[i]);
      c.setObjectives(
          "Aplicar los conceptos del curso en una propuesta académica concreta. Organizar evidencias y comunicar resultados con claridad y rigor.");
      c.setLevel(i == 0 ? "Inicial" : "Intermedio");
      c.setPrice(BigDecimal.valueOf(120 + i * 30));
      c.setDurationHours(24 + i * 6);
      c.setPublished(true);
      c.setCertificateEnabled(true);
      c.getTeachers().add(teacher);
      courses.save(c);
      LearningModule m = new LearningModule();
      m.setCourse(c);
      m.setTitle("Fundamentos y aplicación práctica");
      m.setPosition(1);
      modules.save(m);
      Content intro =
          content(
              m,
              "Bienvenida y ruta de aprendizaje",
              ContentType.TEXTO,
              1,
              true,
              "En este curso conectaremos los conceptos con una propuesta propia. Revisa cada lección, participa en el foro y desarrolla las actividades. El progreso se registra cuando marcas un contenido completado.\n\nLos nombres, cursos y registros de este entorno son datos de demostración y no representan matrículas institucionales reales.");
      content(
          m,
          "Cómo organizar una pregunta de investigación",
          ContentType.VIDEO,
          2,
          true,
          "Una pregunta clara delimita el fenómeno, la población y el contexto. Compara tu pregunta con los objetivos que has propuesto. Puedes vincular un recurso audiovisual HTTPS desde la gestión del curso.");
      content(
          m,
          "Guía de trabajo: estructura del proyecto",
          ContentType.MATERIAL,
          3,
          false,
          "Estructura de trabajo\n1. Tema y contexto.\n2. Problema y objetivos.\n3. Sustento teórico.\n4. Diseño metodológico.\n5. Referencias.\n\nEstas pautas constituyen el material de lectura de la demostración.");
      content(
          m,
          "Actividad: formula tu propuesta",
          ContentType.ACTIVIDAD,
          4,
          false,
          "Presenta un documento con el tema, la pregunta central, un objetivo general y dos objetivos específicos. Explica la relación entre estos elementos. La calificación se registra de 0 a 20.");
      Content forum =
          content(
              m,
              "Foro: de la idea al problema",
              ContentType.FORO,
              5,
              false,
              "¿Qué diferencia una idea general de un problema de investigación delimitado? Comparte una reflexión y responde a un compañero.");
      Discussion post = new Discussion();
      post.setContent(forum);
      post.setAuthor(teacher);
      post.setBody(
          "Bienvenidos. Una buena delimitación identifica qué se estudia, con quiénes y en qué contexto. ¿Cómo lo aplicarías a tu propuesta?");
      post.setCreatedAt(LocalDateTime.now().minusDays(2));
      discussions.save(post);
      Content exam =
          content(
              m,
              "Evaluación formativa de fundamentos",
              ContentType.EVALUACION,
              6,
              false,
              "Resuelve cinco preguntas. Cada una vale dos puntos y se requiere un 70 % para aprobar. Dispones de tres intentos.");
      question(
          exam,
          QuestionType.OPCION_MULTIPLE,
          "¿Qué elemento orienta el desarrollo de una investigación?",
          "La pregunta de investigación\nEl número de páginas\nEl color de la portada",
          "La pregunta de investigación",
          1,
          "La pregunta orienta las decisiones metodológicas.");
      question(
          exam,
          QuestionType.VERDADERO_FALSO,
          "Los objetivos específicos deben contribuir al objetivo general.",
          "",
          "Verdadero",
          2,
          "Los objetivos deben mantener coherencia entre sí.");
      question(
          exam,
          QuestionType.RELACIONAR,
          "Relaciona cada elemento con su función.",
          "Objetivo => Propósito\nMétodo => Procedimiento",
          "Propósito|Procedimiento",
          3,
          "El objetivo expresa un propósito y el método organiza el procedimiento.");
      question(
          exam,
          QuestionType.COMPLETAR,
          "Completa: el proceso sistemático para producir conocimiento se denomina _____.",
          "",
          "Investigación",
          4,
          "La investigación produce conocimiento mediante un proceso sistemático.");
      question(
          exam,
          QuestionType.SELECCIONAR_ESPACIOS,
          "Completa los dos espacios: una _____ propone una explicación; los _____ permiten contrastarla.",
          "Hipótesis\nDatos\nPortada",
          "Hipótesis|Datos",
          5,
          "La hipótesis se contrasta con los datos obtenidos.");
      if (i < 2) {
        enroll(student, c);
        if (i == 0) {
          Progress p = new Progress();
          p.setStudent(student);
          p.setContent(intro);
          p.setCompletedAt(LocalDateTime.now().minusDays(1));
          progress.save(p);
          enroll(second, c);
        }
      }
    }
  }

  private Account account(String name, String email, Role role) {
    Account a = new Account();
    a.setFullName(name);
    a.setEmail(email);
    a.setRole(role);
    a.setActive(true);
    a.setPassword(encoder.encode("Inudi2026!Demo"));
    a.setProfessionalTitle("");
    a.setPhone("");
    return accounts.save(a);
  }

  private Content content(
      LearningModule m,
      String title,
      ContentType type,
      int position,
      boolean preview,
      String body) {
    Content c = new Content();
    c.setModule(m);
    c.setTitle(title);
    c.setType(type);
    c.setPosition(position);
    c.setPreview(preview);
    c.setBody(body);
    c.setMaxAttempts(3);
    c.setPassPercent(70);
    return contents.save(c);
  }

  private void question(
      Content c,
      QuestionType type,
      String prompt,
      String options,
      String answer,
      int order,
      String feedback) {
    Question q = new Question();
    q.setContent(c);
    q.setType(type);
    q.setPrompt(prompt);
    q.setOptionsText(options);
    q.setCorrectAnswer(answer);
    q.setExplanation(feedback);
    q.setPoints(2);
    q.setPosition(order);
    questions.save(q);
  }

  private void enroll(Account a, Course c) {
    Enrollment e = new Enrollment();
    e.setStudent(a);
    e.setCourse(c);
    e.setStatus("ACTIVA");
    e.setStartsAt(LocalDateTime.now().minusDays(7));
    e.setExpiresAt(LocalDateTime.now().plusYears(1));
    enrollments.save(e);
    Payment p = new Payment();
    p.setStudent(a);
    p.setCourse(c);
    p.setAmount(c.getPrice());
    p.setStatus("CONFIRMADO");
    p.setReference("DEMO-" + a.getId() + "-" + c.getId());
    p.setCreatedAt(LocalDateTime.now().minusDays(7));
    p.setConfirmedAt(LocalDateTime.now().minusDays(7));
    payments.save(p);
  }
}
