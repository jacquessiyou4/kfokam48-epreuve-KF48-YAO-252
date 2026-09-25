package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.domain.SourcePresence;
import cm.kfokam48.presence.erreur.ErreurMetierException;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF3 — règles du marquage de présence, avec une horloge injectée. */
class PresenceServiceTest {

    private static final Instant OUVERTURE = Instant.parse("2026-09-25T08:00:00Z");

    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final SessionCoursRepository sessions = mock(SessionCoursRepository.class);
    private final EtudiantRepository etudiants = mock(EtudiantRepository.class);

    private final Promotion promotion = avecId(new Promotion("Promo"), 1L);
    private final Etudiant etudiant = avecId(new Etudiant("Alice", promotion), 10L);
    private final SessionCours session = avecId(new SessionCours("Cours", promotion, "ABCDEF", OUVERTURE), 5L);

    @BeforeEach
    void preparer() {
        when(etudiants.findById(10L)).thenReturn(Optional.of(etudiant));
        when(sessions.findByCode("ABCDEF")).thenReturn(Optional.of(session));
        when(presences.save(any(Presence.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    private PresenceService serviceA(Instant instant) {
        return new PresenceService(presences, sessions, etudiants, Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void codeValide_enregistreUnePresenceEtudiant() {
        var presence = serviceA(OUVERTURE.plusSeconds(60)).marquer(" abcdef ", 10L);

        assertThat(presence.source()).isEqualTo(SourcePresence.ETUDIANT);
        assertThat(presence.sessionId()).isEqualTo(5L);
    }

    @Test
    void rg1_codeExpireApresQuinzeMinutes_renvoie410() {
        assertThatThrownBy(() -> serviceA(OUVERTURE.plusSeconds(15 * 60)).marquer("ABCDEF", 10L))
                .isInstanceOf(ErreurMetierException.class)
                .extracting("code").isEqualTo("CODE_EXPIRE");
        verify(presences, never()).save(any());
    }

    @Test
    void rg2_sessionCloturee_renvoie410MemeAvantQuinzeMinutes() {
        session.cloturer(OUVERTURE.plusSeconds(60));

        assertThatThrownBy(() -> serviceA(OUVERTURE.plusSeconds(120)).marquer("ABCDEF", 10L))
                .extracting("code").isEqualTo("CODE_EXPIRE");
    }

    @Test
    void rg3_dejaPresent_renvoie409() {
        when(presences.existsBySessionIdAndEtudiantId(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> serviceA(OUVERTURE).marquer("ABCDEF", 10L))
                .extracting("code").isEqualTo("DEJA_PRESENT");
    }

    @Test
    void rg4_codeInconnu_renvoie400() {
        assertThatThrownBy(() -> serviceA(OUVERTURE).marquer("ZZZZZZ", 10L))
                .extracting("code").isEqualTo("CODE_INCONNU");
    }

    @Test
    void rg19_etudiantDUneAutrePromotion_renvoie403() {
        Etudiant autre = avecId(new Etudiant("Bob", avecId(new Promotion("Autre"), 2L)), 11L);
        when(etudiants.findById(11L)).thenReturn(Optional.of(autre));

        assertThatThrownBy(() -> serviceA(OUVERTURE).marquer("ABCDEF", 11L))
                .extracting("code").isEqualTo("ETUDIANT_HORS_PROMOTION");
    }

    private static <T> T avecId(T entite, Long id) {
        ReflectionTestUtils.setField(entite, "id", id);
        return entite;
    }
}
