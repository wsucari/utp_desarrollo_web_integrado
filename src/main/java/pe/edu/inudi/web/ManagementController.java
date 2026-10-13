package pe.edu.inudi.web;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.*;

@Controller
@RequestMapping("/gestion")
@RequiredArgsConstructor
public class ManagementController {
  private final AcademicService academic;
  private final StorageService storage;

  @GetMapping("/cursos")
  public String courses(Model m) {
    m.addAttribute("courses", academic.managedCourses());
    m.addAttribute("pageTitle", "Cursos");
    return "manage/courses";
  }

  @GetMapping("/cursos/{id}")
  public String manage(@PathVariable Long id, Model m) {
    var c = academic.managed(id);
    m.addAttribute("course", c);
    m.addAttribute("syllabus", academic.syllabus(id));
    m.addAttribute("pageTitle", c.getTitle());
    return "manage/course";
  }

  @PostMapping("/cursos/{id}/modulos")
  public String module(
      @PathVariable Long id,
      @RequestParam(required = false) Long moduleId,
      @RequestParam String title,
      @RequestParam int position,
      RedirectAttributes f) {
    academic.saveModule(id, moduleId, title, position);
    f.addFlashAttribute("success", "Módulo guardado.");
    return "redirect:/gestion/cursos/" + id;
  }

  @PostMapping("/modulos/{id}/eliminar")
  public String deleteModule(@PathVariable Long id, @RequestParam Long courseId) {
    academic.managed(courseId);
    academic.deleteModule(id);
    return "redirect:/gestion/cursos/" + courseId;
  }

  @GetMapping({"/modulos/{moduleId}/contenidos/nuevo", "/contenidos/{id}/editar"})
  public String contentForm(
      @PathVariable(required = false) Long moduleId,
      @PathVariable(required = false) Long id,
      Model m) {
    ContentForm f = new ContentForm();
    if (id != null) {
      Content c = academic.content(id);
      moduleId = c.getModule().getId();
      f.setTitle(c.getTitle());
      f.setType(c.getType());
      f.setBody(c.getBody());
      f.setResourceUrl(c.getResourceUrl());
      f.setPreview(c.isPreview());
      f.setPosition(c.getPosition());
      f.setMaxAttempts(c.getMaxAttempts());
      f.setPassPercent(c.getPassPercent());
      f.setExamActive(c.isExamActive());
    }
    Long selected = moduleId;
    LearningModule module =
        academic.managedCourses().stream()
            .flatMap(c -> academic.modules(c.getId()).stream())
            .filter(x -> x.getId().equals(selected))
            .findFirst()
            .orElseThrow(
                () ->
                    new org.springframework.security.access.AccessDeniedException(
                        "El módulo no está asignado."));
    m.addAttribute("form", f);
    m.addAttribute("module", module);
    m.addAttribute("id", id);
    m.addAttribute("types", ContentType.values());
    m.addAttribute("pageTitle", id == null ? "Nuevo contenido" : "Editar contenido");
    return "manage/content-form";
  }

  @PostMapping("/modulos/{moduleId}/contenidos")
  public String saveContent(
      @PathVariable Long moduleId,
      @RequestParam(required = false) Long id,
      @Valid @ModelAttribute("form") ContentForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      m.addAttribute("id", id);
      m.addAttribute("types", ContentType.values());
      m.addAttribute(
          "module",
          academic.managedCourses().stream()
              .flatMap(c -> academic.modules(c.getId()).stream())
              .filter(x -> x.getId().equals(moduleId))
              .findFirst()
              .orElseThrow());
      m.addAttribute("pageTitle", "Revisa el contenido");
      return "manage/content-form";
    }
    Content c = academic.saveContent(moduleId, id, f);
    flash.addFlashAttribute("success", "Contenido guardado.");
    return "redirect:/gestion/cursos/" + c.getModule().getCourse().getId();
  }

  @PostMapping("/contenidos/{id}/eliminar")
  public String deleteContent(@PathVariable Long id) {
    Content c = academic.content(id);
    academic.deleteContent(id);
    return "redirect:/gestion/cursos/" + c.getModule().getCourse().getId();
  }

  @PostMapping("/contenidos/{id}/material")
  public String material(
      @PathVariable Long id, @RequestParam MultipartFile file, RedirectAttributes f)
      throws IOException {
    Content c = academic.content(id);
    academic.managed(c.getModule().getCourse().getId());
    var stored = storage.save(file);
    try {
      academic.attachMaterial(id, stored.key(), stored.name());
    } catch (RuntimeException ex) {
      storage.discard(stored);
      throw ex;
    }
    f.addFlashAttribute("success", "Material adjunto disponible en el aula.");
    return "redirect:/gestion/contenidos/" + id + "/editar";
  }

  @GetMapping("/contenidos/{id}/preguntas")
  public String questions(
      @PathVariable Long id, @RequestParam(required = false) Long editar, Model m) {
    Content c = academic.content(id);
    academic.managed(c.getModule().getCourse().getId());
    var list = academic.examQuestions(id);
    QuestionForm f = new QuestionForm();
    if (editar != null) {
      var q =
          list.stream()
              .filter(x -> x.getId().equals(editar))
              .findFirst()
              .orElseThrow(
                  () -> new BusinessException("La pregunta no pertenece a esta evaluación."));
      f.setType(q.getType());
      f.setPrompt(q.getPrompt());
      f.setOptionsText(q.getOptionsText());
      f.setCorrectAnswer(q.getCorrectAnswer());
      f.setExplanation(q.getExplanation());
      f.setPoints(q.getPoints());
      f.setPosition(q.getPosition());
    }
    m.addAttribute("content", c);
    m.addAttribute("questions", list);
    m.addAttribute("form", f);
    m.addAttribute("questionId", editar);
    m.addAttribute("types", QuestionType.values());
    m.addAttribute("pageTitle", "Cuestionario formativo");
    return "manage/questions";
  }

  @PostMapping("/contenidos/{id}/preguntas")
  public String question(
      @PathVariable Long id,
      @RequestParam(required = false) Long questionId,
      @Valid @ModelAttribute("form") QuestionForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (errors.hasErrors()) {
      m.addAttribute("showForm", true);
      Content c = academic.content(id);
      academic.managed(c.getModule().getCourse().getId());
      m.addAttribute("content", c);
      m.addAttribute("questions", academic.examQuestions(id));
      m.addAttribute("types", QuestionType.values());
      m.addAttribute("questionId", questionId);
      m.addAttribute("pageTitle", "Revisa la pregunta");
      return "manage/questions";
    }
    academic.saveQuestion(id, questionId, f);
    flash.addFlashAttribute("success", "Pregunta guardada.");
    return "redirect:/gestion/contenidos/" + id + "/preguntas";
  }

  @PostMapping("/preguntas/{id}/eliminar")
  public String deleteQuestion(@PathVariable Long id, @RequestParam Long contentId) {
    academic.deleteQuestion(id);
    return "redirect:/gestion/contenidos/" + contentId + "/preguntas";
  }

  @GetMapping("/cursos/{id}/seguimiento")
  public String report(@PathVariable Long id, Model m) {
    m.addAttribute("course", academic.managed(id));
    m.addAttribute("roster", academic.roster(id));
    m.addAttribute("submissions", academic.courseSubmissions(id));
    m.addAttribute("attempts", academic.courseAttempts(id));
    m.addAttribute("pageTitle", "Seguimiento académico");
    return "manage/report";
  }

  @PostMapping("/entregas/{id}/calificar")
  public String grade(
      @PathVariable Long id,
      @RequestParam java.math.BigDecimal grade,
      @RequestParam(defaultValue = "") String feedback,
      RedirectAttributes f) {
    var s = academic.readableSubmission(id);
    academic.grade(id, grade, feedback);
    f.addFlashAttribute("success", "Calificación y retroalimentación guardadas.");
    return "redirect:/gestion/cursos/"
        + s.getContent().getModule().getCourse().getId()
        + "/seguimiento";
  }

  @GetMapping("/cursos/{id}/reporte.csv")
  public ResponseEntity<byte[]> csv(@PathVariable Long id) {
    var rows = academic.roster(id);
    StringBuilder csv =
        new StringBuilder("\uFEFFEstudiante;Correo;Curso;Estado;Progreso;Vencimiento\r\n");
    for (var row : rows) {
      Enrollment e = (Enrollment) row.get("enrollment");
      csv.append(
              String.join(
                  ";",
                  cell(e.getStudent().getFullName()),
                  cell(e.getStudent().getEmail()),
                  cell(e.getCourse().getTitle()),
                  cell(e.isCurrent() ? "VIGENTE" : e.getStatus()),
                  cell(row.get("percent") + "%"),
                  cell(e.getExpiresAt().toString())))
          .append("\r\n");
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=seguimiento-" + id + ".csv")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv.toString().getBytes(StandardCharsets.UTF_8));
  }

  private String cell(String value) {
    if (value.matches("^[=+\\-@].*")) value = "'" + value;
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
