package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "course_reviews",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_course_reviews_0",
          columnNames = {"course_id", "author_id"})
    })
@Getter
@Setter
public class CourseReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private Account author;

  @Column(name = "body", nullable = false, length = 2000)
  private String body = "";

  @Column(name = "score", nullable = false)
  private int score;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
