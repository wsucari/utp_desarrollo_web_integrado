package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_ratings_0",
          columnNames = {"post_id", "actor_id"})
    })
@Getter
@Setter
public class Rating {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private Discussion post;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private Account actor;

  @Column(name = "score", nullable = false)
  private int score;
}
