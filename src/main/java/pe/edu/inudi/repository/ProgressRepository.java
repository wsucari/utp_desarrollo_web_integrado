package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
  boolean existsByStudentIdAndContentId(Long studentId, Long contentId);

  List<Progress> findByStudentId(Long studentId);

  @Query(
      "select count(p) from Progress p where p.student.id=:studentId and p.content.module.course.id=:courseId")
  long completed(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

  boolean existsByContentId(Long contentId);
}
