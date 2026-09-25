package cm.kfokam48.presence.dto;

import java.math.BigDecimal;

/** Une ligne de GET /api/tableau : les six champs imposés, plus exercicesEnAttente (H8, Q11). */
public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
        BigDecimal moyenne, int relecturesEnAttente, int exercicesEnAttente) {
}
