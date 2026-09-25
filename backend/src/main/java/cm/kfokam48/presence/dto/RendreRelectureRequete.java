package cm.kfokam48.presence.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * La note est lue en décimal pour pouvoir refuser 12.5 (RG10) : lue en entier, Jackson
 * la tronquerait silencieusement en 12. relecteurId est optionnel (cahier des charges H1).
 */
public record RendreRelectureRequete(BigDecimal note, @NotNull @Size(max = 2000) String commentaire,
        Long relecteurId) {
}
