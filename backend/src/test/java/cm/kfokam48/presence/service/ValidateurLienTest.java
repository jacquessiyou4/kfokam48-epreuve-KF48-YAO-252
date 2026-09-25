package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** RG18 — lien http(s) absolu, moins de 500 caractères. */
class ValidateurLienTest {

    @ParameterizedTest
    @ValueSource(strings = { "https://github.com/moi/exo", "http://exemple.cm/a?b=c", "HTTPS://GITHUB.COM/x" })
    void liensValides(String lien) {
        assertThat(ValidateurLien.estValide(lien)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "pas un lien", "ftp://serveur.cm/exo", "github.com/moi/exo", "https://", "javascript:alert(1)" })
    void liensInvalides(String lien) {
        assertThat(ValidateurLien.estValide(lien)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void lienTropLong() {
        assertThat(ValidateurLien.estValide("https://x.cm/" + "a".repeat(500))).isFalse();
    }
}
