package pe.edu.inudi.api;

import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.service.CatalogService;
import pe.edu.inudi.web.form.QuestionView;

/** Lista explícita de campos públicos. Nunca serializa directamente entidades JPA. */
@Component
public class ApiViews {
  public static Map<String, Object> map(Object... pairs) {
    var result = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
    return result;
  }

  public Object value(Object v) {
    if (v == null || v instanceof String || v instanceof Number || v instanceof Boolean) return v;
    if (v instanceof Enum<?> || v instanceof java.time.temporal.TemporalAccessor)
      return v.toString();
    if (v instanceof Map<?, ?> m) {
      var result = new LinkedHashMap<String, Object>();
      m.forEach((k, x) -> result.put(k.toString(), value(x)));
      return result;
    }
    if (v instanceof Collection<?> c) return c.stream().map(this::value).toList();
    if (v instanceof Account a) return account(a);
    if (v instanceof Course c) return course(c);
    if (v instanceof LearningModule m)
      return map(
          "id",
          m.getId(),
          "title",
          m.getTitle(),
          "position",
          m.getPosition(),
          "courseId",
          m.getCourse().getId());
    if (v instanceof Content c) return content(c, false);
    if (v instanceof Enrollment e)
      return map(
          "id",
          e.getId(),
          "student",
          account(e.getStudent()),
          "course",
          course(e.getCourse()),
          "startsAt",
          e.getStartsAt(),
          "expiresAt",
          e.getExpiresAt(),
          "status",
          e.getStatus(),
          "current",
          e.isCurrent());
    if (v instanceof Payment p)
      return map(
          "id",
          p.getId(),
          "version",
          p.getVersion(),
          "student",
          account(p.getStudent()),
          "course",
          course(p.getCourse()),
          "amount",
          p.getAmount(),
          "reference",
          p.getReference(),
          "status",
          p.getStatus(),
          "createdAt",
          p.getCreatedAt(),
          "expiresAt",
          p.getExpiresAt());
    if (v instanceof CartItem c) return map("id", c.getId(), "course", course(c.getCourse()));
    if (v instanceof Submission s)
      return map(
          "id",
          s.getId(),
          "student",
          account(s.getStudent()),
          "content",
          content(s.getContent(), false),
          "attemptNumber",
          s.getAttemptNumber(),
          "fileName",
          s.getFileName(),
          "comment",
          s.getComment(),
          "grade",
          s.getGrade(),
          "feedback",
          s.getFeedback(),
          "createdAt",
          s.getCreatedAt());
    if (v instanceof Question q) return question(q, false);
    if (v instanceof ExamAttempt a)
      return map(
          "id",
          a.getId(),
          "student",
          account(a.getStudent()),
          "content",
          content(a.getContent(), false),
          "attemptNumber",
          a.getAttemptNumber(),
          "percent",
          a.getPercent(),
          "stars",
          a.getStars(),
          "passed",
          a.isPassed(),
          "createdAt",
          a.getCreatedAt());
    if (v instanceof ExamAnswer a)
      return map(
          "id",
          a.getId(),
          "question",
          question(a.getQuestion(), true),
          "answerText",
          a.getAnswerText(),
          "correct",
          a.isCorrect(),
          "earnedPoints",
          a.getEarnedPoints());
    if (v instanceof Discussion d)
      return map(
          "id",
          d.getId(),
          "author",
          person(d.getAuthor()),
          "content",
          content(d.getContent(), false),
          "parentId",
          d.getParent() == null ? null : d.getParent().getId(),
          "parentAuthor",
          d.getParent() == null ? null : d.getParent().getAuthor().getFullName(),
          "body",
          d.getBody(),
          "deleted",
          d.isDeleted(),
          "createdAt",
          d.getCreatedAt());
    if (v instanceof CourseReview r)
      return map(
          "id",
          r.getId(),
          "author",
          person(r.getAuthor()),
          "body",
          r.getBody(),
          "score",
          r.getScore(),
          "createdAt",
          r.getCreatedAt());
    if (v instanceof Certificate c)
      return map(
          "id",
          c.getId(),
          "courseId",
          c.getCourse().getId(),
          "studentName",
          c.getStudentName(),
          "courseTitle",
          c.getCourseTitle(),
          "code",
          c.getCode(),
          "issuedAt",
          c.getIssuedAt());
    if (v instanceof AuditEntry a)
      return map(
          "id",
          a.getId(),
          "actor",
          account(a.getActor()),
          "action",
          a.getAction(),
          "details",
          a.getDetails(),
          "createdAt",
          a.getCreatedAt());
    if (v instanceof UserSession s)
      return map(
          "id",
          s.getId(),
          "account",
          account(s.getAccount()),
          "roleAtLogin",
          s.getRoleAtLogin(),
          "loggedInAt",
          s.getLoggedInAt(),
          "loggedOutAt",
          s.getLoggedOutAt(),
          "expiresAt",
          s.getExpiresAt(),
          "current",
          s.isCurrent(),
          "duration",
          s.getDuration());
    if (v instanceof CatalogService.Route r)
      return map(
          "slug", r.slug(), "title", r.title(), "label", r.label(), "description", r.description());
    throw new IllegalArgumentException(
        "Tipo de respuesta no previsto: " + v.getClass().getSimpleName());
  }

  public Map<String, Object> person(Account a) {
    return map(
        "id",
        a.getId(),
        "fullName",
        a.getFullName(),
        "initials",
        a.getInitials(),
        "role",
        a.getRole(),
        "professionalTitle",
        a.getProfessionalTitle());
  }

  public Map<String, Object> account(Account a) {
    var result = person(a);
    result.putAll(map("email", a.getEmail(), "phone", a.getPhone(), "active", a.isActive()));
    return result;
  }

  public Map<String, Object> course(Course c) {
    return map(
        "id",
        c.getId(),
        "title",
        c.getTitle(),
        "description",
        c.getDescription(),
        "objectives",
        c.getObjectives(),
        "category",
        c.getCategory(),
        "level",
        c.getLevel(),
        "durationHours",
        c.getDurationHours(),
        "price",
        c.getPrice(),
        "published",
        c.isPublished(),
        "certificateEnabled",
        c.isCertificateEnabled(),
        "teacherNames",
        c.getTeacherNames(),
        "teachers",
        c.getTeachers().stream().map(this::person).toList(),
        "teacherIds",
        c.getTeachers().stream().map(Account::getId).toList());
  }

  public Map<String, Object> content(Content c, boolean detail) {
    var result =
        map(
            "id",
            c.getId(),
            "moduleId",
            c.getModule().getId(),
            "moduleTitle",
            c.getModule().getTitle(),
            "courseId",
            c.getModule().getCourse().getId(),
            "courseTitle",
            c.getModule().getCourse().getTitle(),
            "title",
            c.getTitle(),
            "type",
            c.getType(),
            "position",
            c.getPosition(),
            "preview",
            c.isPreview(),
            "maxAttempts",
            c.getMaxAttempts(),
            "passPercent",
            c.getPassPercent(),
            "examActive",
            c.isExamActive());
    if (detail)
      result.putAll(
          map(
              "body",
              c.getBody(),
              "resourceUrl",
              c.getResourceUrl(),
              "fileName",
              c.getFileName(),
              "hasFile",
              c.getFileKey() != null && !c.getFileKey().isBlank()));
    return result;
  }

  public Map<String, Object> question(Question q, boolean solution) {
    var safe = new QuestionView(q);
    boolean matching = q.getType() == QuestionType.RELACIONAR;
    var result =
        map(
            "id",
            safe.getId(),
            "type",
            safe.getType(),
            "prompt",
            safe.getPrompt(),
            "points",
            safe.getPoints(),
            "position",
            q.getPosition(),
            "options",
            matching && !solution ? List.of() : safe.getOptions(),
            "labels",
            safe.getLabels(),
            "choices",
            matching && !solution
                ? safe.getChoices().stream().sorted().toList()
                : safe.getChoices(),
            "gapCount",
            safe.getGapCount());
    if (solution)
      result.putAll(
          map(
              "optionsText",
              q.getOptionsText(),
              "correctAnswer",
              q.getCorrectAnswer(),
              "explanation",
              q.getExplanation()));
    return result;
  }
}
