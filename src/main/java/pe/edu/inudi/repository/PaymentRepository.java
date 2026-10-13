package pe.edu.inudi.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findAllByOrderByCreatedAtDesc();

  List<Payment> findByStudentIdOrderByCreatedAtDesc(Long studentId);

  boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, String status);

  boolean existsByCourseId(Long courseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.id=:id")
  Optional<Payment> lockById(@Param("id") Long id);
}
