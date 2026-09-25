package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.domain.SessionCours;

/** RG22, RG23 — note retenue et caractère provisoire. */
class NoteRetenueTest {

    private static final Instant T0 = Instant.parse("2026-09-25T08:00:00Z");
    private final Promotion promotion = new Promotion("P");
    private final SessionCours session = new SessionCours("Cours", promotion, "ABCDEF", T0);
    private final Exercice exercice = new Exercice(session, new Etudiant("Auteur", promotion), "https://x.cm", T0);

    private Relecture relecture(Integer note) {
        Relecture r = new Relecture(exercice, new Etudiant("R", promotion), T0);
        if (note != null) {
            r.rendre(note, "ok", T0);
        }
        return r;
    }

    @Test
    void aucuneRelectureRendue_pasDeNote() {
        assertThat(NoteRetenue.de(exercice, List.of(relecture(null), relecture(null)))).isNull();
    }

    @Test
    void uneRelectureSurDeux_noteProvisoire() {
        NoteRetenue note = NoteRetenue.de(exercice, List.of(relecture(12), relecture(null)));

        assertThat(note.valeur()).isEqualByComparingTo("12");
        assertThat(note.provisoire()).isTrue();
    }

    @Test
    void deuxRelectures_moyenneDefinitive() {
        NoteRetenue note = NoteRetenue.de(exercice, List.of(relecture(12), relecture(15)));

        assertThat(note.valeur()).isEqualByComparingTo(new BigDecimal("13.50"));
        assertThat(note.provisoire()).isFalse();
    }

    @Test
    void h13_exerciceDAvantLeChangement_uneSeuleRelectureSuffit() {
        ReflectionTestUtils.setField(exercice, "relecteursRequis", 1);

        assertThat(NoteRetenue.de(exercice, List.of(relecture(15))).provisoire()).isFalse();
    }

    @Test
    void rg17v2_moyenneDesNotesRetenues() {
        assertThat(Moyenne.deNotesRetenues(List.of(new NoteRetenue(new BigDecimal("13.50"), false),
                new NoteRetenue(new BigDecimal("10"), true)))).isEqualByComparingTo(new BigDecimal("11.75"));
        assertThat(Moyenne.deNotesRetenues(List.of())).isNull();
    }
}
