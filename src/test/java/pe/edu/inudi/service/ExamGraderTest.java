package pe.edu.inudi.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import pe.edu.inudi.domain.*;

class ExamGraderTest {
  private final ExamGrader grader = new ExamGrader();

  private Question question(QuestionType type, String key) {
    Question q = new Question();
    q.setType(type);
    q.setCorrectAnswer(key);
    q.setPoints(2);
    return q;
  }

  @Test
  void matchesChoiceExactly() {
    assertThat(grader.correct(question(QuestionType.OPCION_MULTIPLE, "A"), List.of("A"))).isTrue();
    assertThat(grader.correct(question(QuestionType.OPCION_MULTIPLE, "A"), List.of("B"))).isFalse();
  }

  @Test
  void normalizesAccentsAndWhitespaceInGaps() {
    assertThat(
            grader.correct(
                question(QuestionType.COMPLETAR, "Investigación científica"),
                List.of("  INVESTIGACION   cientifica ")))
        .isTrue();
  }

  @Test
  void matchingRequiresAllPairsInOrder() {
    assertThat(grader.correct(question(QuestionType.RELACIONAR, "A|B"), List.of("A", "B")))
        .isTrue();
    assertThat(grader.correct(question(QuestionType.RELACIONAR, "A|B"), List.of("B", "A")))
        .isFalse();
  }

  @Test
  void handlesTrueFalseAndSelectedGaps() {
    assertThat(
            grader.correct(
                question(QuestionType.VERDADERO_FALSO, "Verdadero"), List.of("Verdadero")))
        .isTrue();
    assertThat(
            grader.correct(
                question(QuestionType.SELECCIONAR_ESPACIOS, "Hipótesis|Datos"),
                List.of("Hipótesis", "Datos")))
        .isTrue();
  }

  @Test
  void missingAnswerNeverPasses() {
    assertThat(grader.correct(question(QuestionType.COMPLETAR, ""), List.of())).isFalse();
  }

  @Test
  void starsHaveExactBoundaries() {
    assertThat(grader.stars(49.99)).isZero();
    assertThat(grader.stars(50)).isEqualTo(1);
    assertThat(grader.stars(70)).isEqualTo(2);
    assertThat(grader.stars(90)).isEqualTo(3);
  }
}
