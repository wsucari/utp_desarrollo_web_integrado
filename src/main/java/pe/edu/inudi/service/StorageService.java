package pe.edu.inudi.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {
  private final Path root;

  public StorageService(@Value("${app.storage}") String directory) {
    root = Path.of(directory).toAbsolutePath().normalize();
  }

  public record StoredFile(String key, String name) {}

  public StoredFile save(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty() || file.getSize() > 10 * 1024 * 1024)
      throw new BusinessException("Adjunta un archivo de hasta 10 MB.");
    String name =
        Objects.toString(file.getOriginalFilename(), "archivo")
            .replaceAll("[\\\\/\\r\\n\\p{Cntrl}]", "_");
    if (name.length() > 200) name = name.substring(name.length() - 200);
    int dot = name.lastIndexOf('.');
    String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    if (!Set.of("pdf", "docx", "xlsx", "pptx", "txt", "png", "jpg", "jpeg", "zip").contains(ext))
      throw new BusinessException("Formato admitido: PDF, DOCX, XLSX, PPTX, TXT, PNG, JPG o ZIP.");
    byte[] head;
    try (InputStream in = file.getInputStream()) {
      head = in.readNBytes(8);
    }
    boolean valid =
        switch (ext) {
          case "pdf" ->
              new String(head, java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF-");
          case "docx", "xlsx", "pptx", "zip" ->
              head.length >= 2 && head[0] == 'P' && head[1] == 'K';
          case "png" ->
              head.length >= 4
                  && head[0] == (byte) 137
                  && head[1] == 'P'
                  && head[2] == 'N'
                  && head[3] == 'G';
          case "jpg", "jpeg" -> head.length >= 2 && head[0] == (byte) 255 && head[1] == (byte) 216;
          default -> true;
        };
    if (!valid)
      throw new BusinessException("El contenido del archivo no coincide con su extensión.");
    Files.createDirectories(root);
    String key = UUID.randomUUID() + "." + ext;
    Path target = resolve(key);
    try (InputStream in = file.getInputStream()) {
      Files.copy(in, target);
    }
    return new StoredFile(key, name);
  }

  public Path resolve(String key) {
    if (key == null || !key.matches("[0-9a-f-]{36}\\.(pdf|docx|xlsx|pptx|txt|png|jpe?g|zip)"))
      throw new BusinessException("El archivo no es válido.");
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root)) throw new BusinessException("El archivo no es válido.");
    return path;
  }

  public byte[] read(String key) throws IOException {
    Path file = resolve(key);
    if (!Files.isRegularFile(file)) throw new BusinessException("El archivo no está disponible.");
    return Files.readAllBytes(file);
  }

  public void discard(StoredFile file) throws IOException {
    Files.deleteIfExists(resolve(file.key()));
  }
}
