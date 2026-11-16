package pe.edu.inudi.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.inudi.domain.Enrollment;
import pe.edu.inudi.service.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiFilesController {
  private final AcademicService academic;
  private final StorageService storage;
  private final CertificatePdfService pdf;

  @GetMapping("/contenidos/{id}/archivo")
  public ResponseEntity<byte[]> material(@PathVariable Long id) throws IOException {
    var c = academic.materialForDownload(id);
    return attachment(
        storage.read(c.getFileKey()), c.getFileName(), MediaType.APPLICATION_OCTET_STREAM);
  }

  @GetMapping("/entregas/{id}/archivo")
  public ResponseEntity<byte[]> submission(@PathVariable Long id) throws IOException {
    var s = academic.readableSubmission(id);
    return attachment(
        storage.read(s.getFileKey()), s.getFileName(), MediaType.APPLICATION_OCTET_STREAM);
  }

  @GetMapping("/certificados/{id}/pdf")
  public ResponseEntity<byte[]> certificate(@PathVariable Long id) throws IOException {
    var c = academic.certificate(id);
    return attachment(
        pdf.create(c), "certificado-inudi-" + c.getCode() + ".pdf", MediaType.APPLICATION_PDF);
  }

  @GetMapping("/gestion/cursos/{id}/reporte.csv")
  public ResponseEntity<byte[]> csv(@PathVariable Long id) {
    var csv = new StringBuilder("\uFEFFEstudiante;Correo;Curso;Estado;Progreso;Vencimiento\r\n");
    for (var row : academic.roster(id)) {
      var e = (Enrollment) row.get("enrollment");
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
    return attachment(
        csv.toString().getBytes(StandardCharsets.UTF_8),
        "seguimiento-" + id + ".csv",
        new MediaType("text", "csv", StandardCharsets.UTF_8));
  }

  private String cell(String value) {
    if (value.matches("^[=+\\-@].*")) value = "'" + value;
    return "\"" + value.replace("\"", "\"\"") + "\"";
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
