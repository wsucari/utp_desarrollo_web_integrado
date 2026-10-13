package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
  List<AuditEntry> findTop50ByOrderByCreatedAtDesc();
}
