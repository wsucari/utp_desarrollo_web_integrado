package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
  List<Submission> findByContentIdOrderByCreatedAtDesc(Long contentId);

  List<Submission> findByStudentIdAndContentIdOrderByAttemptNumberDesc(
      Long studentId, Long contentId);

  long countByStudentIdAndContentId(Long studentId, Long contentId);

  boolean existsByContentId(Long contentId);

  @Query(
      "select s from Submission s where s.content.module.course.id=:courseId order by s.createdAt desc")
  List<Submission> inCourse(@Param("courseId") Long courseId);
}
