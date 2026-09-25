package cm.kfokam48.presence.erreur;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Gestion centralisée des erreurs (B4) : toute erreur sort au format { code, message },
 * jamais de stack trace ni de page d'erreur Spring.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

    private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreurs.class);

    @ExceptionHandler(ErreurMetierException.class)
    ResponseEntity<ErreurDto> metier(ErreurMetierException e) {
        return reponse(e.getStatut(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION", "Requête invalide — " + detail + ".");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErreurDto> corpsIlisible(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION",
                "Le corps de la requête est absent ou n'est pas un JSON valide.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErreurDto> parametreManquant(MissingServletRequestParameterException e) {
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION",
                "Le paramètre « " + e.getParameterName() + " » est obligatoire.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErreurDto> typeInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(HttpStatus.BAD_REQUEST, "VALIDATION",
                "La valeur de « " + e.getName() + " » n'a pas le bon format.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErreurDto> routeInconnue(NoResourceFoundException e) {
        return reponse(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", "Cette adresse n'existe pas.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErreurDto> methodeNonAutorisee(HttpRequestMethodNotSupportedException e) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED, "METHODE_NON_AUTORISEE",
                "La méthode " + e.getMethod() + " n'est pas autorisée sur cette adresse.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErreurDto> typeDeContenu(HttpMediaTypeNotSupportedException e) {
        return reponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TYPE_NON_SUPPORTE",
                "Le corps de la requête doit être en application/json.");
    }

    /** Filet de sécurité si deux requêtes simultanées violent une contrainte d'unicité (RG3, RG12). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErreurDto> conflit(DataIntegrityViolationException e) {
        LOG.warn("Contrainte d'intégrité violée", e);
        return reponse(HttpStatus.CONFLICT, "CONFLIT",
                "L'opération entre en conflit avec des données existantes.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErreurDto> inattendue(Exception e) {
        LOG.error("Erreur inattendue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Une erreur inattendue est survenue. Réessayez plus tard.");
    }

    private static ResponseEntity<ErreurDto> reponse(HttpStatus statut, String code, String message) {
        return ResponseEntity.status(statut).body(new ErreurDto(code, message));
    }
}
