package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
  List<ExamAttempt> findByStudentIdAndContentIdOrderByAttemptNumberDesc(
      Long studentId, Long contentId);

  List<ExamAttempt> findByContentIdOrderByCreatedAtDesc(Long contentId);

  List<ExamAttempt> findByStudentIdOrderByCreatedAtDesc(Long studentId);

  long countByStudentIdAndContentId(Long studentId, Long contentId);

  boolean existsByContentId(Long contentId);

  @Query(
      "select a from ExamAttempt a where a.content.module.course.id=:courseId order by a.createdAt desc")
  List<ExamAttempt> inCourse(@Param("courseId") Long courseId);
}
