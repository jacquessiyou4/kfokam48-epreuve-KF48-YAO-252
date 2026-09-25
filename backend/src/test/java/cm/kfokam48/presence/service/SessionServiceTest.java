package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.dto.SessionOuverteDto;
import cm.kfokam48.presence.erreur.ErreurMetierException;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

class SessionServiceTest {

    private static final Instant MAINTENANT = Instant.parse("2026-09-25T08:00:00Z");

    private final SessionCoursRepository sessions = mock(SessionCoursRepository.class);
    private final PromotionRepository promotions = mock(PromotionRepository.class);
    private final GenerateurCode generateur = mock(GenerateurCode.class);
    private SessionService service;

    @BeforeEach
    void preparer() {
        service = new SessionService(sessions, promotions, generateur, Clock.fixed(MAINTENANT, ZoneOffset.UTC));
        when(promotions.findById(1L)).thenReturn(Optional.of(new Promotion("Promo test")));
        when(sessions.save(any(SessionCours.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void rg1_leCodeExpireQuinzeMinutesApresLOuverture() {
        when(generateur.generer()).thenReturn("ABCDEF");

        SessionOuverteDto session = service.ouvrir("Cours", 1L);

        assertThat(session.ouvertureAt()).isEqualTo(MAINTENANT);
        assertThat(session.expirationAt()).isEqualTo(Instant.parse("2026-09-25T08:15:00Z"));
    }

    @Test
    void rg4_unCodeDejaPrisEstRegenere() {
        when(generateur.generer()).thenReturn("PRIS22", "LIBRE3");
        when(sessions.existsByCode("PRIS22")).thenReturn(true);

        assertThat(service.ouvrir("Cours", 1L).code()).isEqualTo("LIBRE3");
    }

    @Test
    void promotionInconnue_renvoie404() {
        assertThatThrownBy(() -> service.ouvrir("Cours", 99L))
                .isInstanceOf(ErreurMetierException.class)
                .extracting("code").isEqualTo("PROMOTION_INCONNUE");
    }

    @Test
    void rg1_leCodeNEstPlusUtilisableAuBoutDeQuinzeMinutes() {
        SessionCours session = new SessionCours("Cours", new Promotion("P"), "ABCDEF", MAINTENANT);

        assertThat(session.codeUtilisableA(MAINTENANT.plusSeconds(14 * 60 + 59))).isTrue();
        assertThat(session.codeUtilisableA(MAINTENANT.plusSeconds(15 * 60))).isFalse();
    }

    @Test
    void rg2_leCodeNEstPlusUtilisableApresCloture() {
        SessionCours session = new SessionCours("Cours", new Promotion("P"), "ABCDEF", MAINTENANT);
        session.cloturer(MAINTENANT.plusSeconds(60));

        assertThat(session.codeUtilisableA(MAINTENANT.plusSeconds(120))).isFalse();
    }
}
