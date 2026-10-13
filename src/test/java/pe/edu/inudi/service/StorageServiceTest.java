package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class StorageServiceTest {
  @TempDir Path directory;

  @Test
  void storesPrivateFileWithGeneratedKey() throws Exception {
    var storage = new StorageService(directory.toString());
    var file =
        storage.save(
            new MockMultipartFile(
                "file", "../propuesta.txt", "text/plain", "Una propuesta".getBytes()));
    assertThat(file.key()).matches("[0-9a-f-]{36}\\.txt");
    assertThat(file.name()).doesNotContain("/");
    assertThat(new String(storage.read(file.key()))).isEqualTo("Una propuesta");
  }

  @Test
  void rejectsExecutableFiles() {
    assertThatThrownBy(
            () ->
                new StorageService(directory.toString())
                    .save(
                        new MockMultipartFile(
                            "file", "app.exe", "application/octet-stream", new byte[] {1})))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void rejectsDisguisedPdf() {
    assertThatThrownBy(
            () ->
                new StorageService(directory.toString())
                    .save(
                        new MockMultipartFile(
                            "file", "app.pdf", "application/pdf", "not a PDF".getBytes())))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void rejectsPathTraversal() {
    assertThatThrownBy(() -> new StorageService(directory.toString()).resolve("../../secrets.txt"))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void rejectsEmptyAndOversizedFiles() {
    var storage = new StorageService(directory.toString());
    assertThatThrownBy(
            () ->
                storage.save(new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0])))
        .isInstanceOf(BusinessException.class);
    assertThatThrownBy(
            () ->
                storage.save(
                    new MockMultipartFile(
                        "file", "big.txt", "text/plain", new byte[10 * 1024 * 1024 + 1])))
        .isInstanceOf(BusinessException.class);
  }
}
