package pe.edu.inudi.service;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.inudi.domain.UserSession;
import pe.edu.inudi.repository.*;

@Service @RequiredArgsConstructor
public class SessionService {
  private final UserSessionRepository sessions;
  private final AccountRepository accounts;
  private final AcademicService academic;
  private static final String KEY = SessionService.class.getName() + ".record";
  @Transactional
  public void start(HttpSession http, String email) {
    UserSession record = new UserSession();
    var account = accounts.findByEmailIgnoreCase(email).orElseThrow();
    record.setAccount(account); record.setRoleAtLogin(account.getRole().name());
    record.setLoggedInAt(LocalDateTime.now());
    record.setExpiresAt(LocalDateTime.now().plusSeconds(http.getMaxInactiveInterval()));
    http.setAttribute(KEY, sessions.save(record).getId());
  }
  @Transactional
  public void touch(HttpSession http) {
    if (http != null && http.getAttribute(KEY) instanceof Long id)
      sessions.touch(id, LocalDateTime.now().plusSeconds(http.getMaxInactiveInterval()));
  }
  @Transactional
  public void close(HttpSession http) {
    if (http != null && http.getAttribute(KEY) instanceof Long id) sessions.close(id, LocalDateTime.now());
  }
  @Transactional(readOnly = true)
  public List<UserSession> list(String search, String state) {
    academic.admin();
    if (search.length() > 180) throw new BusinessException("La búsqueda supera el tamaño permitido.");
    String query = search.toLowerCase(java.util.Locale.ROOT);
    return sessions.findAllByOrderByLoggedInAtDesc().stream()
      .filter(s -> state.isBlank() || (state.equals("en sesion") == s.isCurrent()))
      .filter(s -> (s.getAccount().getFullName() + " " + s.getAccount().getEmail())
          .toLowerCase(java.util.Locale.ROOT).contains(query)).toList();
  }
}
