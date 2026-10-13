package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
  List<Discussion> findByContentIdOrderByCreatedAtAsc(Long contentId);

  boolean existsByContentId(Long contentId);
  List<Discussion> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
  List<Discussion> findAllByOrderByCreatedAtDesc();
}
