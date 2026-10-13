package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findByStudentIdOrderByIdDesc(Long studentId);

  Optional<CartItem> findByStudentIdAndCourseId(Long studentId, Long courseId);

  void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
