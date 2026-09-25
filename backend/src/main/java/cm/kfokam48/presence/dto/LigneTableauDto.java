package cm.kfokam48.presence.dto;

import java.math.BigDecimal;

/**
 * Une ligne de GET /api/tableau : les six champs imposés, plus exercicesEnAttente (H8, Q11)
 * et moyenneProvisoire (v2, RG17 : vrai si l'une des notes retenues est provisoire).
 */
public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
        BigDecimal moyenne, int relecturesEnAttente, int exercicesEnAttente, boolean moyenneProvisoire) {
}
