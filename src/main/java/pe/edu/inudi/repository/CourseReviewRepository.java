package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
  Optional<CourseReview> findByCourseIdAndAuthorId(Long courseId, Long authorId);

  List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
