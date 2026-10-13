package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
  Optional<Certificate> findByCode(String code);

  Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);

  List<Certificate> findByStudentIdOrderByIssuedAtDesc(Long studentId);

  List<Certificate> findAllByOrderByIssuedAtDesc();

  boolean existsByCode(String code);
}
