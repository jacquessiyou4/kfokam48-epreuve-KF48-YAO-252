package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.domain.StatutExercice;
import cm.kfokam48.presence.erreur.ErreurMetierException;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF8 — RG6, RG10, RG11, RG20 sur la relecture. */
class RelectureServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-25T08:00:00Z");

    private final RelectureRepository relectures = mock(RelectureRepository.class);
    private final RelectureService service = new RelectureService(relectures, mock(EtudiantRepository.class),
            Clock.fixed(T0, ZoneOffset.UTC));

    private final Promotion promotion = avecId(new Promotion("P"), 1L);
    private final SessionCours session = avecId(new SessionCours("Cours", promotion, "ABCDEF", T0), 5L);
    private final Etudiant auteur = avecId(new Etudiant("Auteur", promotion), 1L);
    private final Etudiant relecteur = avecId(new Etudiant("Relecteur", promotion), 2L);
    private final Exercice exercice = avecId(new Exercice(session, auteur, "https://x.cm", T0), 9L);
    private final Relecture relecture = avecId(new Relecture(exercice, relecteur, T0), 3L);

    private final Etudiant secondRelecteur = avecId(new Etudiant("Second", promotion), 4L);
    private final Relecture secondeRelecture = avecId(new Relecture(exercice, secondRelecteur, T0), 6L);

    @BeforeEach
    void preparer() {
        exercice.marquerRelecteurAffecte();
        when(relectures.findById(3L)).thenReturn(Optional.of(relecture));
        when(relectures.findById(6L)).thenReturn(Optional.of(secondeRelecture));
        when(relectures.findByExerciceIdOrderByIdAsc(9L)).thenReturn(List.of(relecture, secondeRelecture));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "12", "20", "15.0" })
    void rg10_noteEntiereDe0A20_acceptee(String note) {
        var rendue = service.rendre(3L, new BigDecimal(note), "Bien", 2L);

        assertThat(rendue.rendue()).isTrue();
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.RELU_PARTIELLEMENT);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "21", "12.5", "0.1" })
    void rg10_noteHorsBornesOuNonEntiere_renvoieNoteInvalide(String note) {
        assertThatThrownBy(() -> service.rendre(3L, new BigDecimal(note), "Bien", 2L))
                .isInstanceOf(ErreurMetierException.class)
                .extracting("code").isEqualTo("NOTE_INVALIDE");
        assertThat(relecture.estRendue()).isFalse();
    }

    @Test
    void rg23_premiereRelecture_reluPartiellement_puisSeconde_relu() {
        service.rendre(3L, BigDecimal.valueOf(12), "Premier avis", 2L);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.RELU_PARTIELLEMENT);

        service.rendre(6L, BigDecimal.valueOf(15), "Second avis", 4L);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.RELU);
    }

    @Test
    void rg10_noteAbsente_renvoieNoteInvalide() {
        assertThatThrownBy(() -> service.rendre(3L, null, "Bien", 2L)).extracting("code").isEqualTo("NOTE_INVALIDE");
    }

    @Test
    void rg6_lAuteurNePeutPasRelireSonPropreExercice() {
        assertThatThrownBy(() -> service.rendre(3L, BigDecimal.TEN, "Moi", 1L))
                .extracting("code").isEqualTo("AUTO_RELECTURE");
    }

    @Test
    void h1_unAutreEtudiantQueLeRelecteurAssigneEstRefuse() {
        assertThatThrownBy(() -> service.rendre(3L, BigDecimal.TEN, "Moi", 42L))
                .extracting("code").isEqualTo("RELECTEUR_NON_ASSIGNE");
    }

    @Test
    void rg11_uneRelectureRendueEstDefinitive() {
        service.rendre(3L, BigDecimal.TEN, "Première", null);

        assertThatThrownBy(() -> service.rendre(3L, BigDecimal.ONE, "Seconde", null))
                .extracting("code").isEqualTo("RELECTURE_DEJA_RENDUE");
        assertThat(relecture.getNote()).isEqualTo(10);
    }

    @Test
    void rg20_sessionCloturee_renvoieSessionCloturee() {
        session.cloturer(T0);

        assertThatThrownBy(() -> service.rendre(3L, BigDecimal.TEN, "Trop tard", 2L))
                .extracting("code").isEqualTo("SESSION_CLOTUREE");
    }

    private static <T> T avecId(T entite, Long id) {
        ReflectionTestUtils.setField(entite, "id", id);
        return entite;
    }
}
