package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "discussions")
@Getter
@Setter
public class Discussion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private Account author;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "parent_id", nullable = true)
  private Discussion parent;

  @Column(name = "body", nullable = false, length = 4000)
  private String body = "";

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
