package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OuvrirSessionRequete(
        @NotBlank @Size(max = 200) String titre,
        @NotNull Long promotionId) {
}
