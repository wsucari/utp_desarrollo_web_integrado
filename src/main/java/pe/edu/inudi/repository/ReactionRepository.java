package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.inudi.domain.*;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
  Optional<Reaction> findByPostIdAndActorId(Long postId, Long actorId);

  long countByPostId(Long postId);
}
