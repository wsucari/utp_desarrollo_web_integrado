package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class Question {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private QuestionType type;

  @Column(name = "prompt", nullable = false, length = 2000)
  private String prompt = "";

  @Column(name = "options_text", nullable = false, length = 3000)
  private String optionsText = "";

  @Column(name = "correct_answer", nullable = false, length = 2000)
  private String correctAnswer = "";

  @Column(name = "explanation", nullable = false, length = 2000)
  private String explanation = "";

  @Column(name = "points", nullable = false)
  private int points;

  @Column(name = "position", nullable = false)
  private int position;

  public List<String> getOptions() {
    return optionsText.isBlank() ? List.of() : Arrays.asList(optionsText.split("\\n"));
  }

  public List<String> getLabels() {
    return getOptions().stream().map(s -> s.split("=>", 2)[0].trim()).toList();
  }

  public List<String> getChoices() {
    return getOptions().stream()
        .map(s -> s.contains("=>") ? s.split("=>", 2)[1].trim() : s.trim())
        .toList();
  }
}
