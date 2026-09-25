package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/** RG5 (Q4) — 5 codes erronés consécutifs : blocage de 2 minutes, avec une horloge qu'on fait avancer. */
class LimiteurTentativesTest {

    static class HorlogeReglable extends Clock {
        Instant maintenant = Instant.parse("2026-09-25T08:00:00Z");

        @Override
        public Instant instant() {
            return maintenant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final HorlogeReglable horloge = new HorlogeReglable();
    private final LimiteurTentatives limiteur = new LimiteurTentatives(horloge);

    private void echouer(int fois) {
        for (int i = 0; i < fois; i++) {
            limiteur.enregistrerEchec(1L);
        }
    }

    @Test
    void quatreEchecs_pasDeBlocage() {
        echouer(4);
        assertThatCode(() -> limiteur.verifierNonBloque(1L)).doesNotThrowAnyException();
    }

    @Test
    void cinqEchecs_bloquePendantDeuxMinutesPuisDebloque() {
        echouer(5);
        assertThatThrownBy(() -> limiteur.verifierNonBloque(1L)).extracting("code").isEqualTo("TROP_DE_TENTATIVES");

        horloge.maintenant = horloge.maintenant.plusSeconds(119);
        assertThatThrownBy(() -> limiteur.verifierNonBloque(1L)).extracting("code").isEqualTo("TROP_DE_TENTATIVES");

        horloge.maintenant = horloge.maintenant.plusSeconds(1);
        assertThatCode(() -> limiteur.verifierNonBloque(1L)).doesNotThrowAnyException();
    }

    @Test
    void unSuccesRemetLeCompteurAZero() {
        echouer(4);
        limiteur.reinitialiser(1L);
        echouer(4);
        assertThatCode(() -> limiteur.verifierNonBloque(1L)).doesNotThrowAnyException();
    }

    @Test
    void h10_leBlocageNeConcerneQueLEtudiantFautif() {
        echouer(5);
        assertThatCode(() -> limiteur.verifierNonBloque(2L)).doesNotThrowAnyException();
    }
}
