package cm.kfokam48.presence.erreur;

import org.springframework.http.HttpStatus;

/**
 * Violation d'une règle de gestion. Chaque erreur porte son statut HTTP et son code stable
 * (voir la liste des codes dans api/contrat.yaml).
 */
public class ErreurMetierException extends RuntimeException {

    private final HttpStatus statut;
    private final String code;

    public ErreurMetierException(HttpStatus statut, String code, String message) {
        super(message);
        this.statut = statut;
        this.code = code;
    }

    public HttpStatus getStatut() {
        return statut;
    }

    public String getCode() {
        return code;
    }
}
