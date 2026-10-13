package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "exam_answers",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_exam_answers_0",
          columnNames = {"attempt_id", "question_id"})
    })
@Getter
@Setter
public class ExamAnswer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "attempt_id", nullable = false)
  private ExamAttempt attempt;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "answer_text", nullable = false, length = 3000)
  private String answerText = "";

  @Column(name = "correct", nullable = false)
  private boolean correct;

  @Column(name = "earned_points", nullable = false)
  private int earnedPoints;
}
