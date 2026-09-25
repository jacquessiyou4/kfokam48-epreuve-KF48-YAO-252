package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.domain.Promotion;

public record PromotionDto(Long id, String nom) {

    public static PromotionDto de(Promotion p) {
        return new PromotionDto(p.getId(), p.getNom());
    }
}
