package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "progress",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_progress_0",
          columnNames = {"student_id", "content_id"})
    })
@Getter
@Setter
public class Progress {
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

  @Column(name = "completed_at", nullable = false)
  private LocalDateTime completedAt;
}
