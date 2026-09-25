package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.RepeatedTest;

/** ENF6 — code lisible : 6 caractères, majuscules et chiffres, sans 0 O 1 I L. */
class GenerateurCodeTest {

    private final GenerateurCode generateur = new GenerateurCode();

    @RepeatedTest(200)
    void codeDeSixCaracteresSansCaractereAmbigu() {
        String code = generateur.generer();

        assertThat(code).hasSize(6).matches("[A-Z2-9]{6}").doesNotContain("0", "O", "1", "I", "L");
    }
}
