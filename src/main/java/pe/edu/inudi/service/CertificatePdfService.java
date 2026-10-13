package pe.edu.inudi.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.inudi.domain.Certificate;

@Service
public class CertificatePdfService {
  private final String publicUrl;
  private final boolean demo;

  @org.springframework.beans.factory.annotation.Autowired
  public CertificatePdfService(
      @Value("${app.public-url}") String publicUrl, @Value("${app.demo.enabled}") boolean demo) {
    this.publicUrl = publicUrl.replaceAll("/$", "");
    this.demo = demo;
  }

  public CertificatePdfService(String publicUrl) {
    this(publicUrl, false);
  }

  public byte[] create(Certificate cert) throws IOException {
    try (PDDocument doc = new PDDocument();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      PDPage page = new PDPage(new PDRectangle(842, 595));
      doc.addPage(page);
      PDFont normal = font(doc, false), bold = font(doc, true);
      try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
        canvas.setNonStrokingColor(0.06f, 0.15f, 0.28f);
        canvas.addRect(0, 0, 842, 595);
        canvas.fill();
        canvas.setNonStrokingColor(1f, 1f, 1f);
        canvas.addRect(20, 20, 802, 555);
        canvas.fill();
        canvas.setStrokingColor(0.7f, 0.55f, 0.25f);
        canvas.setLineWidth(1.5f);
        canvas.addRect(36, 36, 770, 523);
        canvas.stroke();
        write(canvas, bold, 26, 70, 510, "INUDI PERÚ");
        write(
            canvas,
            normal,
            11,
            70,
            484,
            "Instituto Universitario de Innovación Ciencia y Tecnología");
        write(canvas, bold, 32, 70, 413, "CERTIFICADO DE PARTICIPACIÓN");
        write(
            canvas,
            normal,
            14,
            70,
            372,
            "Se reconoce la participación y culminación del curso de:");
        textBox(canvas, bold, 24, 70, 344, 700, 66, cert.getStudentName());
        write(canvas, normal, 13, 70, 280, "quien completó los contenidos académicos del programa");
        textBox(canvas, bold, 20, 70, 245, 570, 66, cert.getCourseTitle());
        write(
            canvas,
            normal,
            12,
            70,
            161,
            "Emitido el "
                + cert.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " · Código "
                + cert.getCode());
        write(
            canvas,
            normal,
            10,
            70,
            137,
            "Validación: " + publicUrl + "/cert/" + cert.getCode());
        write(canvas, normal, 10, 70, 84, "Investigar para un mundo mejor · Inudi Perú");
        if (demo)
          write(
              canvas,
              bold,
              8,
              70,
              61,
              "DEMOSTRACIÓN LOCAL · NO ACREDITA UNA CERTIFICACIÓN INSTITUCIONAL REAL");
        try {
          ByteArrayOutputStream qr = new ByteArrayOutputStream();
          MatrixToImageWriter.writeToStream(
              new QRCodeWriter()
                  .encode(
                      publicUrl + "/cert/" + cert.getCode(), BarcodeFormat.QR_CODE, 160, 160),
              "PNG",
              qr);
          canvas.drawImage(
              PDImageXObject.createFromByteArray(doc, qr.toByteArray(), "validacion"),
              665,
              70,
              105,
              105);
        } catch (WriterException ex) {
          throw new IOException("No fue posible generar el QR", ex);
        }
      }
      doc.save(output);
      return output.toByteArray();
    }
  }

  private PDFont font(PDDocument doc, boolean bold) throws IOException {
    for (String name :
        new String[] {
          bold ? "C:/Windows/Fonts/arialbd.ttf" : "C:/Windows/Fonts/arial.ttf",
          bold
              ? "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
              : "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        }) {
      if (Files.isRegularFile(Path.of(name)))
        try (InputStream in = Files.newInputStream(Path.of(name))) {
          return PDType0Font.load(doc, in);
        }
    }
    return new PDType1Font(
        bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
  }

  private void write(PDPageContentStream c, PDFont font, int size, float x, float y, String text)
      throws IOException {
    c.setNonStrokingColor(0.08f, 0.18f, 0.3f);
    c.beginText();
    c.setFont(font, size);
    c.newLineAtOffset(x, y);
    c.showText(safe(font, text));
    c.endText();
  }

  private String safe(PDFont font, String text) throws IOException {
    StringBuilder result = new StringBuilder();
    for (int cp : text.codePoints().toArray()) {
      String s = new String(Character.toChars(cp));
      try {
        font.encode(s);
        result.append(s);
      } catch (IllegalArgumentException ex) {
        result.append("?");
      }
    }
    return result.toString();
  }

  private java.util.List<String> lines(PDFont f, int size, float width, String text)
      throws IOException {
    var result = new java.util.ArrayList<String>();
    StringBuilder line = new StringBuilder();
    for (int cp : text.codePoints().toArray()) {
      String s = new String(Character.toChars(cp));
      if (line.length() > 0 && f.getStringWidth(line + s) / 1000 * size > width) {
        int space = line.lastIndexOf(" ");
        if (space > 0) {
          result.add(line.substring(0, space));
          String remainder = line.substring(space + 1);
          line = new StringBuilder(remainder);
        } else {
          result.add(line.toString());
          line = new StringBuilder();
        }
      }
      line.append(s);
    }
    if (!line.isEmpty()) result.add(line.toString());
    return result;
  }

  private void textBox(
      PDPageContentStream c,
      PDFont f,
      int preferred,
      float x,
      float y,
      float width,
      float height,
      String text)
      throws IOException {
    text = safe(f, text);
    int size = preferred;
    var rows = lines(f, size, width, text);
    while (size > 8 && rows.size() * (size + 5) > height) {
      rows = lines(f, --size, width, text);
    }
    for (String row : rows) {
      write(c, f, size, x, y, row);
      y -= size + 5;
    }
  }
}
