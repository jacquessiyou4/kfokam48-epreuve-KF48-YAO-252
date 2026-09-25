package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** RG17, RG22 — moyennes arithmétiques arrondies à 2 décimales, null sans note. */
final class Moyenne {

    private Moyenne() {
    }

    static BigDecimal de(List<Integer> notes) {
        if (notes.isEmpty()) {
            return null;
        }
        int somme = notes.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(somme).divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);
    }

    /** RG17 v2 — moyenne d'un étudiant = moyenne de ses notes retenues. */
    static BigDecimal deNotesRetenues(List<NoteRetenue> notes) {
        if (notes.isEmpty()) {
            return null;
        }
        BigDecimal somme = notes.stream().map(NoteRetenue::valeur).reduce(BigDecimal.ZERO, BigDecimal::add);
        return somme.divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);
    }
}
