package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.domain.Relecture;

/** Vue du relecteur : jamais le nom de l'auteur. */
public record RelectureAFaireDto(Long id, Long exerciceId, String sessionTitre, String lien,
        boolean rendue, Integer note, String commentaire) {

    public static RelectureAFaireDto de(Relecture r) {
        return new RelectureAFaireDto(r.getId(), r.getExercice().getId(), r.getExercice().getSession().getTitre(),
                r.getExercice().getLien(), r.estRendue(), r.getNote(), r.getCommentaire());
    }
}
