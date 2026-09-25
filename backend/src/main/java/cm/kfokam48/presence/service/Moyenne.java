package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** RG17 — moyenne arithmétique des notes reçues, arrondie à 2 décimales, null sans note. */
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
}
