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

    public static ErreurMetierException lienInvalide() {
        return new ErreurMetierException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
                "Le lien doit être une adresse web complète commençant par http:// ou https://.");
    }

    public static ErreurMetierException exerciceDejaDepose() {
        return new ErreurMetierException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
                "Un exercice a déjà été déposé par cet étudiant pour cette session.");
    }

    public static ErreurMetierException sessionCloturee() {
        return new ErreurMetierException(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
                "La session est clôturée : cette opération n'est plus possible.");
    }

    public static ErreurMetierException relectureInconnue(Long id) {
        return new ErreurMetierException(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
                "La relecture " + id + " n'existe pas.");
    }

    public static ErreurMetierException noteInvalide() {
        return new ErreurMetierException(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE",
                "La note doit être un nombre entier compris entre 0 et 20.");
    }

    public static ErreurMetierException autoRelecture() {
        return new ErreurMetierException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE",
                "Un étudiant ne peut pas relire son propre exercice.");
    }

    public static ErreurMetierException relecteurNonAssigne() {
        return new ErreurMetierException(HttpStatus.FORBIDDEN, "RELECTEUR_NON_ASSIGNE",
                "Cette relecture est assignée à un autre étudiant.");
    }

    public static ErreurMetierException relectureDejaRendue() {
        return new ErreurMetierException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE",
                "Cette relecture a déjà été rendue : elle est définitive.");
    }

    public static ErreurMetierException sessionDejaCloturee() {
        return new ErreurMetierException(HttpStatus.CONFLICT, "SESSION_DEJA_CLOTUREE",
                "Cette session est déjà clôturée.");
    }
}
