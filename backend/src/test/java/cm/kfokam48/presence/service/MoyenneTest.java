package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/** RG17 — moyenne des notes reçues, arrondie à 2 décimales, null sans note. */
class MoyenneTest {

    @Test
    void sansNote_null() {
        assertThat(Moyenne.de(List.of())).isNull();
    }

    @Test
    void arrondiADeuxDecimales() {
        assertThat(Moyenne.de(List.of(10, 11, 11))).isEqualByComparingTo(new BigDecimal("10.67"));
        assertThat(Moyenne.de(List.of(15, 12))).isEqualByComparingTo(new BigDecimal("13.50"));
        assertThat(Moyenne.de(List.of(0))).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
