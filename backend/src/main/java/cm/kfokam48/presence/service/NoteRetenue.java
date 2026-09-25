package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.util.List;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Relecture;

/**
 * RG22 — note retenue d'un exercice = moyenne des notes de ses relectures rendues (2 décimales).
 * RG23 — provisoire tant que toutes les relectures requises ne sont pas rendues.
 */
public record NoteRetenue(BigDecimal valeur, boolean provisoire) {

    /** @return null si aucune relecture n'est rendue */
    public static NoteRetenue de(Exercice exercice, List<Relecture> relecturesDeLExercice) {
        List<Integer> notes = relecturesDeLExercice.stream()
                .filter(Relecture::estRendue)
                .map(Relecture::getNote)
                .toList();
        if (notes.isEmpty()) {
            return null;
        }
        return new NoteRetenue(Moyenne.de(notes), notes.size() < exercice.getRelecteursRequis());
    }
}
