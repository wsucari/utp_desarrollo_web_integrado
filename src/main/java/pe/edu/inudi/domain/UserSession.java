package pe.edu.inudi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_sessions")
@Getter @Setter
public class UserSession {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "account_id", nullable = false) private Account account;
  @Column(name = "role_at_login", nullable = false, length = 30) private String roleAtLogin;
  @Column(name = "logged_in_at", nullable = false) private LocalDateTime loggedInAt;
  @Column(name = "logged_out_at") private LocalDateTime loggedOutAt;
  @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
  public boolean isCurrent() { return loggedOutAt == null && expiresAt.isAfter(LocalDateTime.now()); }
  public String getDuration() {
    long seconds = java.time.Duration.between(loggedInAt,
        loggedOutAt != null ? loggedOutAt : LocalDateTime.now().isBefore(expiresAt) ? LocalDateTime.now() : expiresAt).getSeconds();
    return String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
  }
}
