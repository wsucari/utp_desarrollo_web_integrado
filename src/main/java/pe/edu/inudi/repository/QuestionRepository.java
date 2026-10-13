package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface QuestionRepository extends JpaRepository<Question, Long> {
  List<Question> findByContentIdOrderByPositionAscIdAsc(Long contentId);

  void deleteByContentId(Long contentId);
}
