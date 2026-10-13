package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {
  List<LearningModule> findByCourseIdOrderByPositionAscIdAsc(Long courseId);
}
