package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.domain.Exercice;
import jakarta.persistence.LockModeType;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /**
     * RG9 v2 : exercices de la session qui ont moins de relecteurs que requis.
     * Bug #32 : verrou d'écriture (SELECT ... FOR UPDATE) pour que deux présences simultanées
     * ne complètent pas le même exercice ; la seconde attend la fin de la première.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Exercice e where e.session.id = :sessionId"
            + " and (select count(r) from Relecture r where r.exercice = e) < e.relecteursRequis")
    List<Exercice> verrouillerSansAssezDeRelecteurs(@Param("sessionId") Long sessionId);

    List<Exercice> findBySessionPromotionId(Long promotionId);

    List<Exercice> findByEtudiantIdOrderByDeposeAtDesc(Long etudiantId);
}
