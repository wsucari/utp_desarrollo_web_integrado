package pe.edu.inudi.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.inudi.domain.*;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByPostIdAndActorId(Long postId, Long actorId);

  @Query("select avg(r.score) from Rating r where r.post.id=:postId")
  Double average(@Param("postId") Long postId);
}
