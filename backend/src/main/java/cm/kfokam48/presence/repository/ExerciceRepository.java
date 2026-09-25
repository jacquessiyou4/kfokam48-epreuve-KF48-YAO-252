package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);

    List<Exercice> findBySessionPromotionId(Long promotionId);

    List<Exercice> findByEtudiantIdOrderByDeposeAtDesc(Long etudiantId);
}
