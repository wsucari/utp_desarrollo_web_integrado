package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface CourseRepository extends JpaRepository<Course, Long> {
  @Query(
      "select distinct c from Course c where c.published=true and (:q='' or lower(c.title) like lower(concat('%',:q,'%'))) and (:category='' or c.category=:category) order by c.id desc")
  List<Course> catalog(@Param("q") String q, @Param("category") String category);

  @Query(
      "select distinct c from Course c join c.teachers t where t.id=:teacherId order by c.id desc")
  List<Course> assigned(@Param("teacherId") Long teacherId);

  List<Course> findAllByOrderByIdDesc();
}
