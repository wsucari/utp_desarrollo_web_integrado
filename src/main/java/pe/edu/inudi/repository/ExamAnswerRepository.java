package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
  List<ExamAnswer> findByAttemptIdOrderByQuestionPositionAscQuestionIdAsc(Long attemptId);
}
