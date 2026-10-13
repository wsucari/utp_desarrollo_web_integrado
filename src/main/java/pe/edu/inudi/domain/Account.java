package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_accounts_0",
          columnNames = {"email"})
    })
@Getter
@Setter
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @Column(name = "full_name", nullable = false, length = 120)
  private String fullName = "";

  @Column(name = "email", nullable = false, length = 180)
  private String email = "";

  @Column(name = "password", nullable = false, length = 100)
  private String password = "";

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private Role role;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "professional_title", nullable = true, length = 180)
  private String professionalTitle;

  @Column(name = "phone", nullable = true, length = 30)
  private String phone;

  public String getInitials() {
    String[] w = fullName.trim().split("\\s+");
    return (w[0].substring(0, 1) + (w.length > 1 ? w[w.length - 1].substring(0, 1) : ""))
        .toUpperCase();
  }
}
