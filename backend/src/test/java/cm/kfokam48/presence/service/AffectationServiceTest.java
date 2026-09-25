package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.domain.SourcePresence;
import cm.kfokam48.presence.domain.StatutExercice;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF6 — tirage du relecteur : RG6 (jamais l'auteur), RG8 (parmi les présents), RG9 (sinon en attente). */
class AffectationServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-25T08:00:00Z");

    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final RelectureRepository relectures = mock(RelectureRepository.class);
    private final AffectationService service = new AffectationService(presences, mock(ExerciceRepository.class),
            relectures, new SecureRandom(), Clock.fixed(T0, ZoneOffset.UTC));

    private final Promotion promotion = avecId(new Promotion("P"), 1L);
    private final SessionCours session = avecId(new SessionCours("Cours", promotion, "ABCDEF", T0), 5L);
    private final Etudiant auteur = avecId(new Etudiant("Auteur", promotion), 1L);
    private final Etudiant camarade = avecId(new Etudiant("Camarade", promotion), 2L);
    private final Etudiant autreCamarade = avecId(new Etudiant("Autre", promotion), 3L);

    private void presents(Etudiant... etudiants) {
        when(presences.findBySessionId(5L)).thenReturn(
                java.util.Arrays.stream(etudiants).map(e -> new Presence(session, e, SourcePresence.ETUDIANT, T0)).toList());
    }

    @RepeatedTest(100)
    void rg6_rg8_lAuteurPresentNEstJamaisTireAuSort() {
        presents(auteur, camarade);
        Exercice exercice = new Exercice(session, auteur, "https://x.cm", T0);

        service.affecter(exercice);

        ArgumentCaptor<Relecture> relecture = ArgumentCaptor.forClass(Relecture.class);
        verify(relectures).save(relecture.capture());
        assertThat(relecture.getValue().getRelecteur()).isSameAs(camarade);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }

    @RepeatedTest(50)
    void rg7v2_deuxRelecteursDifferentsJamaisLAuteur() {
        presents(auteur, camarade, autreCamarade);
        Exercice exercice = new Exercice(session, auteur, "https://x.cm", T0);

        service.affecter(exercice);

        ArgumentCaptor<Relecture> relecture = ArgumentCaptor.forClass(Relecture.class);
        verify(relectures, times(2)).save(relecture.capture());
        List<Etudiant> tires = relecture.getAllValues().stream().map(Relecture::getRelecteur).toList();
        assertThat(tires).containsExactlyInAnyOrder(camarade, autreCamarade);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }

    @Test
    void rg8_leTirageEstAleatoireParmiPlusDeDeuxEligibles() {
        Etudiant quatrieme = avecId(new Etudiant("Quatrième", promotion), 4L);
        presents(auteur, camarade, autreCamarade, quatrieme);
        ArgumentCaptor<Relecture> relecture = ArgumentCaptor.forClass(Relecture.class);

        for (int i = 0; i < 100; i++) {
            service.affecter(new Exercice(session, auteur, "https://x.cm", T0));
        }

        verify(relectures, times(200)).save(relecture.capture());
        assertThat(relecture.getAllValues().stream().map(Relecture::getRelecteur).distinct().toList())
                .containsExactlyInAnyOrder(camarade, autreCamarade, quatrieme);
    }

    @Test
    void rg9v2_unRelecteurDejaAffecteNEstPasRetireEtIlNEnManqueQuUn() {
        presents(auteur, camarade, autreCamarade);
        Exercice exercice = avecId(new Exercice(session, auteur, "https://x.cm", T0), 9L);
        when(relectures.findByExerciceIdOrderByIdAsc(9L)).thenReturn(List.of(new Relecture(exercice, camarade, T0)));

        service.affecter(exercice);

        ArgumentCaptor<Relecture> relecture = ArgumentCaptor.forClass(Relecture.class);
        verify(relectures).save(relecture.capture());
        assertThat(relecture.getValue().getRelecteur()).isSameAs(autreCamarade);
    }

    @Test
    void rg9_auteurSeulPresent_lExerciceResteEnAttenteDeRelecteur() {
        presents(auteur);
        Exercice exercice = new Exercice(session, auteur, "https://x.cm", T0);

        service.affecter(exercice);

        verify(relectures, never()).save(any());
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTEUR);
    }

    private static <T> T avecId(T entite, Long id) {
        ReflectionTestUtils.setField(entite, "id", id);
        return entite;
    }
}
