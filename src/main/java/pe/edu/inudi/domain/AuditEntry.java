package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_entries")
@Getter
@Setter
public class AuditEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private Account actor;

  @Column(name = "action", nullable = false, length = 80)
  private String action = "";

  @Column(name = "details", nullable = false, length = 1000)
  private String details = "";

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
