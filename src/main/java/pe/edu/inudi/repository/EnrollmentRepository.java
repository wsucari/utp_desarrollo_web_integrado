package pe.edu.inudi.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Enrollment e where e.student.id=:studentId and e.course.id=:courseId")
  Optional<Enrollment> lockForStudent(
      @Param("studentId") Long studentId, @Param("courseId") Long courseId);

  List<Enrollment> findByStudentIdOrderByIdDesc(Long studentId);

  List<Enrollment> findByCourseIdOrderByIdDesc(Long courseId);

  List<Enrollment> findAllByOrderByIdDesc();

  boolean existsByCourseId(Long courseId);
}
