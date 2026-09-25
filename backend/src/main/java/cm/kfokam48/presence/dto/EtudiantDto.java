package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.domain.Etudiant;

public record EtudiantDto(Long id, String nom, Long promotionId) {

    public static EtudiantDto de(Etudiant e) {
        return new EtudiantDto(e.getId(), e.getNom(), e.getPromotion().getId());
    }
}
