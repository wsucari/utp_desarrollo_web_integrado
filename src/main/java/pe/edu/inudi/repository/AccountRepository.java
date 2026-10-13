package pe.edu.inudi.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface AccountRepository extends JpaRepository<Account, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Account a where a.id=:id")
  Optional<Account> lockById(@Param("id") Long id);

  Optional<Account> findByEmailIgnoreCase(String email);

  List<Account> findByRoleOrderByFullName(Role role);

  long countByRoleAndActiveTrue(Role role);
}
