package pe.edu.inudi.repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.UserSession;
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  List<UserSession> findAllByOrderByLoggedInAtDesc();
  @Modifying @Query("update UserSession s set s.loggedOutAt=:time where s.id=:id and s.loggedOutAt is null")
  int close(Long id, LocalDateTime time);
  @Modifying @Query("update UserSession s set s.expiresAt=:time where s.id=:id and s.loggedOutAt is null")
  int touch(Long id, LocalDateTime time);
}
