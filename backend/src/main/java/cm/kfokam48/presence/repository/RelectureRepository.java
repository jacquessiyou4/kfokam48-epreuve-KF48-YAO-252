package cm.kfokam48.presence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.domain.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    boolean existsByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurIdOrderByRendueAtAscAssigneeAtAsc(Long relecteurId);

    /** Tableau (ENF2) : l'exercice est chargé avec la relecture, pas une requête par ligne. */
    @Query("select r from Relecture r join fetch r.exercice e where e.session.promotion.id = :promotionId")
    List<Relecture> findAvecExerciceParPromotion(@Param("promotionId") Long promotionId);
}
