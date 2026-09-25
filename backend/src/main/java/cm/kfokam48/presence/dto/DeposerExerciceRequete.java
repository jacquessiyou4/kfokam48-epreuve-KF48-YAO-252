package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotNull;

/** Le lien n'est pas annoté : absent ou mal formé, il donne 400 LIEN_INVALIDE (contrat). */
public record DeposerExerciceRequete(@NotNull Long sessionId, @NotNull Long etudiantId, String lien) {
}
