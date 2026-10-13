package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "certificates",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_certificates_0",
          columnNames = {"student_id", "course_id"}),
      @UniqueConstraint(
          name = "uq_certificates_1",
          columnNames = {"code"})
    })
@Getter
@Setter
public class Certificate {
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

  @Column(name = "code", nullable = false, length = 7)
  private String code = "";

  @Column(name = "student_name", nullable = false, length = 120)
  private String studentName = "";

  @Column(name = "course_title", nullable = false, length = 180)
  private String courseTitle = "";

  @Column(name = "issued_at", nullable = false)
  private LocalDateTime issuedAt;
}
