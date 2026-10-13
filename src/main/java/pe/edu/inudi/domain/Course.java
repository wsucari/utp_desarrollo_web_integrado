package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @Column(name = "title", nullable = false, length = 180)
  private String title = "";

  @Column(name = "description", nullable = false, length = 6000)
  private String description = "";

  @Column(name = "objectives", nullable = false, length = 2000)
  private String objectives = "";

  @Column(name = "category", nullable = false, length = 100)
  private String category = "";

  @Column(name = "level", nullable = false, length = 50)
  private String level = "";

  @Column(name = "duration_hours", nullable = false)
  private int durationHours;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "published", nullable = false)
  private boolean published;

  @Column(name = "certificate_enabled", nullable = false)
  private boolean certificateEnabled;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "course_teachers",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  private Set<Account> teachers = new LinkedHashSet<>();

  public String getTeacherNames() {
    return teachers.stream()
        .map(Account::getFullName)
        .collect(java.util.stream.Collectors.joining(", "));
  }
}
