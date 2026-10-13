package pe.edu.inudi.service;

import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Component;
import pe.edu.inudi.domain.Question;

@Component
public class ExamGrader {
  public String normalize(String text) {
    return Normalizer.normalize(Objects.toString(text, ""), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .replaceAll("\\s+", " ")
        .toLowerCase(Locale.ROOT);
  }

  public boolean correct(Question question, List<String> answers) {
    if (answers == null || answers.isEmpty() || question.getCorrectAnswer().isBlank()) return false;
    var expected =
        Arrays.stream(question.getCorrectAnswer().split("\\|", -1)).map(this::normalize).toList();
    return expected.equals(answers.stream().map(this::normalize).toList());
  }

  public int stars(double percent) {
    return percent >= 90 ? 3 : percent >= 70 ? 2 : percent >= 50 ? 1 : 0;
  }
}
