package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "exam_attempts",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_exam_attempts_0",
          columnNames = {"student_id", "content_id", "attempt_number"})
    })
@Getter
@Setter
public class ExamAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Account student;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(name = "attempt_number", nullable = false)
  private int attemptNumber;

  @Column(name = "percent", nullable = false, precision = 10, scale = 2)
  private BigDecimal percent;

  @Column(name = "stars", nullable = false)
  private int stars;

  @Column(name = "passed", nullable = false)
  private boolean passed;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
