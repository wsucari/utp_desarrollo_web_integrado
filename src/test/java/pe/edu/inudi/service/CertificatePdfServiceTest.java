package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.time.LocalDateTime;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import pe.edu.inudi.domain.Certificate;

class CertificatePdfServiceTest {
  @Test
  void longNamesAndTitlesRemainOnSinglePage() throws Exception {
    Certificate c = new Certificate();
    c.setStudentName("W".repeat(120));
    c.setCourseTitle("W".repeat(180));
    c.setCode("7654321");
    c.setIssuedAt(LocalDateTime.now());
    try (var pdf =
        Loader.loadPDF(new CertificatePdfService("http://localhost:8080", true).create(c))) {
      assertThat(pdf.getNumberOfPages()).isEqualTo(1);
      assertThat(new PDFTextStripper().getText(pdf)).contains("7654321", "DEMOSTRACIÓN LOCAL");
    }
  }

  @Test
  void pdfContainsParticipantCourseCodeAndVerifiableQr() throws Exception {
    Certificate c = new Certificate();
    c.setStudentName("José Núñez");
    c.setCourseTitle("Metodología de la investigación");
    c.setCode("1234567");
    c.setIssuedAt(LocalDateTime.now());
    byte[] bytes = new CertificatePdfService("http://127.0.0.1:8080").create(c);
    try (var pdf = Loader.loadPDF(bytes)) {
      assertThat(pdf.getNumberOfPages()).isEqualTo(1);
      String text = new PDFTextStripper().getText(pdf);
      assertThat(text).contains("José Núñez", "1234567", "Metodología");
      var resources = pdf.getPage(0).getResources();
      boolean found = false;
      for (var name : resources.getXObjectNames())
        if (resources.getXObject(name) instanceof PDImageXObject image) {
          var bitmap =
              new BinaryBitmap(
                  new HybridBinarizer(new BufferedImageLuminanceSource(image.getImage())));
          assertThat(new MultiFormatReader().decode(bitmap).getText())
              .isEqualTo("http://127.0.0.1:8080/cert/1234567");
          found = true;
        }
      assertThat(found).isTrue();
    }
  }
}
