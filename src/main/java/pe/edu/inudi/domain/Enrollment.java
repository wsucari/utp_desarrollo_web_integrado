package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_enrollments_0",
          columnNames = {"student_id", "course_id"})
    })
@Getter
@Setter
public class Enrollment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Account student;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "starts_at", nullable = false)
  private LocalDateTime startsAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "status", nullable = false, length = 30)
  private String status = "";

  public boolean isCurrent() {
    var now = LocalDateTime.now();
    return "ACTIVA".equals(status)
        && !startsAt.isAfter(now)
        && expiresAt.isAfter(now)
        && student.isActive();
  }
}
