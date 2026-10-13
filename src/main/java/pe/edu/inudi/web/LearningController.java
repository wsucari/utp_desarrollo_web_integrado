package pe.edu.inudi.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.service.*;
import pe.edu.inudi.web.form.QuestionView;

@Controller
@RequiredArgsConstructor
public class LearningController {
  private final AcademicService academic;
  private final StorageService storage;
  private final CertificatePdfService pdf;

  @GetMapping("/aula/cursos/{id}")
  public String classroom(@PathVariable Long id, Model m) {
    Course c = academic.course(id);
    if (!academic.hasAccess(c, academic.required()))
      throw new AccessDeniedException("La matrícula no está vigente.");
    m.addAttribute("course", c);
    m.addAttribute("syllabus", academic.syllabus(id));
    m.addAttribute("completed", academic.completedIds());
    m.addAttribute("percent", academic.percent(academic.required().getId(), id));
    m.addAttribute("canManage", academic.canManage(c, academic.required()));
    m.addAttribute("pageTitle", c.getTitle());
    return "classroom";
  }

  @GetMapping({"/contenidos/{id}", "/vista-previa/{id}"})
  public String lesson(@PathVariable Long id, Model m) {
    Content c = academic.readable(id);
    Account a = academic.current();
    boolean access = academic.hasAccess(c.getModule().getCourse(), a);
    boolean manage = academic.canManage(c.getModule().getCourse(), a);
    m.addAttribute("content", c);
    m.addAttribute("course", c.getModule().getCourse());
    m.addAttribute("syllabus", academic.syllabus(c.getModule().getCourse().getId()));
    m.addAttribute("canLearn", access);
    m.addAttribute("canManage", manage);
    m.addAttribute("completed", a == null ? Set.of() : academic.completedIds());
    m.addAttribute("posts", access ? academic.posts(id) : List.of());
    if (access && c.getType() == ContentType.ACTIVIDAD)
      m.addAttribute("submissions", academic.contentSubmissions(id));
    if (access && c.getType() == ContentType.EVALUACION) {
      m.addAttribute(
          "questions", academic.examQuestions(id).stream().map(QuestionView::new).toList());
      m.addAttribute("attempts", academic.examAttempts(id));
    }
    if (c.getType() == ContentType.VIDEO && c.getResourceUrl() != null) {
      java.util.regex.Matcher yt =
          java.util.regex.Pattern.compile(
                  "^https://(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_-]{11})(?:[?&].*)?$")
              .matcher(c.getResourceUrl());
      if (yt.matches())
        m.addAttribute("embedUrl", "https://www.youtube-nocookie.com/embed/" + yt.group(1));
    }
    m.addAttribute("pageTitle", c.getTitle());
    return "lesson";
  }

  @PostMapping("/contenidos/{id}/completar")
  public String complete(@PathVariable Long id, RedirectAttributes f) {
    academic.complete(id);
    f.addFlashAttribute("success", "Progreso guardado.");
    return "redirect:/contenidos/" + id;
  }

  @PostMapping("/contenidos/{id}/entregar")
  public String submit(
      @PathVariable Long id,
      @RequestParam MultipartFile file,
      @RequestParam(defaultValue = "") String comment,
      RedirectAttributes f)
      throws IOException {
    academic.student();
    academic.readable(id);
    var stored = storage.save(file);
    try {
      academic.submit(id, stored.key(), stored.name(), comment);
    } catch (RuntimeException ex) {
      storage.discard(stored);
      throw ex;
    }
    f.addFlashAttribute(
        "success", "Actividad enviada. Puedes consultar aquí la nota y la retroalimentación.");
    return "redirect:/contenidos/" + id;
  }

  @GetMapping("/entregas/{id}/archivo")
  public ResponseEntity<byte[]> submissionFile(@PathVariable Long id) throws IOException {
    var s = academic.readableSubmission(id);
    return attachment(
        storage.read(s.getFileKey()), s.getFileName(), MediaType.APPLICATION_OCTET_STREAM);
  }

  @GetMapping("/contenidos/{id}/archivo")
  public ResponseEntity<byte[]> material(@PathVariable Long id) throws IOException {
    var c = academic.materialForDownload(id);
    return attachment(
        storage.read(c.getFileKey()), c.getFileName(), MediaType.APPLICATION_OCTET_STREAM);
  }

  @PostMapping("/contenidos/{id}/evaluar")
  public String evaluate(@PathVariable Long id, @RequestParam MultiValueMap<String, String> form) {
    var a = academic.evaluate(id, form);
    return "redirect:/evaluaciones/intentos/" + a.getId();
  }

  @GetMapping("/evaluaciones/intentos/{id}")
  public String result(@PathVariable Long id, Model m) {
    var a = academic.attempt(id);
    m.addAttribute("attempt", a);
    m.addAttribute("answers", academic.result(id));
    m.addAttribute("pageTitle", "Tu resultado formativo");
    return "result";
  }

  @PostMapping("/contenidos/{id}/publicaciones")
  public String post(
      @PathVariable Long id,
      @RequestParam(required = false) Long parentId,
      @RequestParam String body) {
    academic.post(id, parentId, body);
    return "redirect:/contenidos/" + id + "#participacion";
  }

  @PostMapping("/publicaciones/{id}/editar")
  public String editPost(
      @PathVariable Long id, @RequestParam String body, @RequestParam Long contentId) {
    academic.editPost(id, body);
    return "redirect:/contenidos/" + contentId + "#participacion";
  }

  @PostMapping("/publicaciones/{id}/eliminar")
  public String deletePost(@PathVariable Long id, @RequestParam Long contentId) {
    academic.deletePost(id);
    return "redirect:/contenidos/" + contentId + "#participacion";
  }

  @PostMapping("/publicaciones/{id}/reaccion")
  public String react(
      @PathVariable Long id, @RequestParam String type, @RequestParam Long contentId) {
    academic.react(id, type);
    return "redirect:/contenidos/" + contentId + "#participacion";
  }

  @PostMapping("/publicaciones/{id}/valorar")
  public String rate(@PathVariable Long id, @RequestParam int score, @RequestParam Long contentId) {
    academic.rate(id, score);
    return "redirect:/contenidos/" + contentId + "#participacion";
  }

  @PostMapping("/cursos/{id}/resenas")
  public String review(@PathVariable Long id, @RequestParam int score, @RequestParam String body) {
    academic.review(id, score, body);
    return "redirect:/catalogo/" + id + "#resenas";
  }

  @PostMapping("/resenas/{id}/eliminar")
  public String removeReview(@PathVariable Long id, @RequestParam Long courseId) {
    academic.deleteReview(id);
    return "redirect:/catalogo/" + courseId + "#resenas";
  }

  @GetMapping("/certificados/{id}/pdf")
  public ResponseEntity<byte[]> certificate(@PathVariable Long id) throws IOException {
    var c = academic.certificate(id);
    return attachment(
        pdf.create(c), "certificado-inudi-" + c.getCode() + ".pdf", MediaType.APPLICATION_PDF);
  }

  private ResponseEntity<byte[]> attachment(byte[] bytes, String name, MediaType type) {
    return ResponseEntity.ok()
        .contentType(type)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(name, StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(bytes);
  }
}
