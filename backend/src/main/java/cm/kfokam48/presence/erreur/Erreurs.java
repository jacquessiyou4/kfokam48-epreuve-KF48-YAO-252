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

    public static ErreurMetierException codeInconnu() {
        return new ErreurMetierException(HttpStatus.BAD_REQUEST, "CODE_INCONNU",
                "Ce code de présence ne correspond à aucune session.");
    }

    public static ErreurMetierException codeExpire() {
        return new ErreurMetierException(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
    }

    public static ErreurMetierException dejaPresent() {
        return new ErreurMetierException(HttpStatus.CONFLICT, "DEJA_PRESENT",
                "La présence de cet étudiant est déjà enregistrée pour cette session.");
    }

    public static ErreurMetierException etudiantHorsPromotion() {
        return new ErreurMetierException(HttpStatus.FORBIDDEN, "ETUDIANT_HORS_PROMOTION",
                "Cet étudiant n'appartient pas à la promotion de cette session.");
    }
}
