package cm.kfokam48.presence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.domain.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurIdOrderByRendueAtAscAssigneeAtAsc(Long relecteurId);

    List<Relecture> findByExerciceSessionPromotionId(Long promotionId);
}
