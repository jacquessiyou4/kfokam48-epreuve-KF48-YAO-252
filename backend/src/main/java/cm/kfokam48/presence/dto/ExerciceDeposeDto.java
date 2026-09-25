package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.StatutExercice;

/** Réponse imposée de POST /api/exercices. */
public record ExerciceDeposeDto(Long id, StatutExercice statut) {

    public static ExerciceDeposeDto de(Exercice e) {
        return new ExerciceDeposeDto(e.getId(), e.getStatut());
    }
}
