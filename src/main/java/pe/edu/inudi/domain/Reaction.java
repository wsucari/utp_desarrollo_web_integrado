package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_reactions_0",
          columnNames = {"post_id", "actor_id"})
    })
@Getter
@Setter
public class Reaction {
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

  @Column(name = "type", nullable = false, length = 30)
  private String type = "";
}
