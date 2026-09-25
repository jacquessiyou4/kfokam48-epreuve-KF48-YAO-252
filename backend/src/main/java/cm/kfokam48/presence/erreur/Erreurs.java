package cm.kfokam48.presence.erreur;

import org.springframework.http.HttpStatus;

/** Fabrique des erreurs métier : un seul endroit pour les codes et les messages du contrat. */
public final class Erreurs {

    private Erreurs() {
    }

    public static ErreurMetierException promotionInconnue(Long id) {
        return new ErreurMetierException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE",
                "La promotion " + id + " n'existe pas.");
    }

    public static ErreurMetierException sessionInconnue(Long id) {
        return new ErreurMetierException(HttpStatus.NOT_FOUND, "SESSION_INCONNUE",
                "La session " + id + " n'existe pas.");
    }

    public static ErreurMetierException etudiantInconnu(Long id) {
        return new ErreurMetierException(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU",
                "L'étudiant " + id + " n'existe pas.");
    }
}
