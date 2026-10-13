package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contents")
@Getter
@Setter
public class Content {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "module_id", nullable = false)
  private LearningModule module;

  @Column(name = "title", nullable = false, length = 180)
  private String title = "";

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private ContentType type;

  @Column(name = "body", nullable = false, length = 12000)
  private String body = "";

  @Column(name = "resource_url", nullable = true, length = 1000)
  private String resourceUrl;

  @Column(name = "file_key", nullable = true, length = 100)
  private String fileKey;

  @Column(name = "file_name", nullable = true, length = 200)
  private String fileName;

  @Column(name = "preview", nullable = false)
  private boolean preview;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

  @Column(name = "pass_percent", nullable = false)
  private int passPercent;

  @Column(name="exam_active",nullable=false)
  private boolean examActive = true;
}
