package cm.kfokam48.presence.dto;

import java.math.BigDecimal;

/**
 * Une ligne de GET /api/tableau : les six champs imposés, plus exercicesEnAttente (H8, Q11)
 * moyenneProvisoire (v2, RG17 : vrai si l'une des notes retenues est provisoire) et
 * presencesAjouteesParFormateur (Q14 : « il faut que ça se voie », RG15).
 */
public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
        BigDecimal moyenne, int relecturesEnAttente, int exercicesEnAttente, boolean moyenneProvisoire,
        int presencesAjouteesParFormateur) {
}
