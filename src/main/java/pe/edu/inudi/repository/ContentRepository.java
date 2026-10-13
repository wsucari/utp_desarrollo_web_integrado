package pe.edu.inudi.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface ContentRepository extends JpaRepository<Content, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Content c where c.id=:id")
  Optional<Content> lockById(@Param("id") Long id);

  List<Content> findByModuleIdOrderByPositionAscIdAsc(Long moduleId);

  @Query(
      "select c from Content c where c.module.course.id=:courseId order by c.module.position,c.module.id,c.position,c.id")
  List<Content> inCourse(@Param("courseId") Long courseId);

  long countByModuleCourseId(Long courseId);
}
