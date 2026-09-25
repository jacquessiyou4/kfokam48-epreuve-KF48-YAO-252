package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Promotion;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.dto.SessionDto;
import cm.kfokam48.presence.dto.SessionOuverteDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

@Service
public class SessionService {

    /** Au-delà, l'espace des codes (31^6) est anormalement saturé : on échoue plutôt que boucler. */
    private static final int ESSAIS_MAX_CODE = 20;

    private final SessionCoursRepository sessions;
    private final PromotionRepository promotions;
    private final GenerateurCode generateurCode;
    private final Clock horloge;

    public SessionService(SessionCoursRepository sessions, PromotionRepository promotions,
            GenerateurCode generateurCode, Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.generateurCode = generateurCode;
        this.horloge = horloge;
    }

    /** EF1 — le code expire 15 minutes après l'ouverture (RG1) et désigne une seule session (RG4). */
    @Transactional
    public SessionOuverteDto ouvrir(String titre, Long promotionId) {
        Promotion promotion = promotions.findById(promotionId)
                .orElseThrow(() -> Erreurs.promotionInconnue(promotionId));
        SessionCours session = sessions.save(
                new SessionCours(titre.trim(), promotion, codeUnique(), horloge.instant()));
        return new SessionOuverteDto(session.getId(), session.getCode(),
                session.getOuvertureAt(), session.getExpirationAt());
    }

    @Transactional(readOnly = true)
    public List<SessionDto> lister(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw Erreurs.promotionInconnue(promotionId);
        }
        return sessions.findByPromotionIdOrderByOuvertureAtDesc(promotionId).stream()
                .map(SessionDto::de)
                .toList();
    }

    /** EF10 — la clôture fige présences (RG2), dépôts (RG13) et relectures (RG20). */
    @Transactional
    public SessionDto cloturer(Long sessionId) {
        SessionCours session = sessions.findById(sessionId)
                .orElseThrow(() -> Erreurs.sessionInconnue(sessionId));
        if (session.estCloturee()) {
            throw Erreurs.sessionDejaCloturee();
        }
        session.cloturer(horloge.instant());
        return SessionDto.de(session);
    }

    private String codeUnique() {
        for (int i = 0; i < ESSAIS_MAX_CODE; i++) {
            String code = generateurCode.generer();
            if (!sessions.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code de présence unique");
    }
}
