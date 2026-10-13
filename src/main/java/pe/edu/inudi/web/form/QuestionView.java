package pe.edu.inudi.web.form;

import java.util.List;
import lombok.Getter;
import pe.edu.inudi.domain.*;

/** Modelo de lectura que no expone las respuestas correctas al estudiante. */
@Getter
public class QuestionView {
  private final Long id;
  private final QuestionType type;
  private final String prompt;
  private final int points;
  private final List<String> options;
  private final List<String> labels;
  private final List<String> choices;
  private final int gapCount;

  public QuestionView(Question q) {
    id = q.getId();
    type = q.getType();
    prompt = q.getPrompt();
    points = q.getPoints();
    options = q.getOptions();
    labels = q.getLabels();
    choices = q.getChoices();
    gapCount = q.getCorrectAnswer().split("\\|", -1).length;
  }
}
