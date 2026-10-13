package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "submissions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_submissions_0",
          columnNames = {"student_id", "content_id", "attempt_number"})
    })
@Getter
@Setter
public class Submission {
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

  @Column(name = "file_key", nullable = false, length = 100)
  private String fileKey = "";

  @Column(name = "file_name", nullable = false, length = 200)
  private String fileName = "";

  @Column(name = "comment", nullable = false, length = 1000)
  private String comment = "";

  @Column(name = "grade", nullable = true, precision = 10, scale = 2)
  private BigDecimal grade;

  @Column(name = "feedback", nullable = true, length = 1000)
  private String feedback;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
