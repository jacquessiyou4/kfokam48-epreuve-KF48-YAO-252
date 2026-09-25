package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.StatutExercice;
import jakarta.persistence.LockModeType;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /**
     * Bug #32 : verrou d'écriture (SELECT ... FOR UPDATE) pour que deux présences simultanées
     * n'affectent pas le même exercice en attente ; la seconde attend la fin de la première.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Exercice e where e.session.id = :sessionId and e.statut = :statut")
    List<Exercice> verrouillerParSessionEtStatut(@Param("sessionId") Long sessionId,
            @Param("statut") StatutExercice statut);

    List<Exercice> findBySessionPromotionId(Long promotionId);

    List<Exercice> findByEtudiantIdOrderByDeposeAtDesc(Long etudiantId);
}
