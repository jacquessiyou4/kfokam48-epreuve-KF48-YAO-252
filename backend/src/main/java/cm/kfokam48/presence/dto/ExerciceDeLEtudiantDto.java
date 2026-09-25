package cm.kfokam48.presence.dto;

import java.math.BigDecimal;
import java.util.List;

import cm.kfokam48.presence.domain.StatutExercice;

/** EF11 v2 — ce que l'auteur voit de son exercice : jamais les relecteurs (Q8, RG16). */
public record ExerciceDeLEtudiantDto(Long id, Long sessionId, String sessionTitre, String lien,
        StatutExercice statut, BigDecimal noteRetenue, boolean noteProvisoire, List<String> commentaires) {
}
