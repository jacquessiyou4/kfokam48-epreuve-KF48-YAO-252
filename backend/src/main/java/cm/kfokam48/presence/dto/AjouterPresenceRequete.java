package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotNull;

public record AjouterPresenceRequete(@NotNull Long etudiantId) {
}
