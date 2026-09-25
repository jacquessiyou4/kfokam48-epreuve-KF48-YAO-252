package cm.kfokam48.presence.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.domain.SourcePresence;
import cm.kfokam48.presence.dto.PresenceDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

@Service
public class PresenceService {

    private final PresenceRepository presences;
    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final AffectationService affectation;
    private final Clock horloge;

    public PresenceService(PresenceRepository presences, SessionCoursRepository sessions,
            EtudiantRepository etudiants, AffectationService affectation, Clock horloge) {
        this.presences = presences;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.affectation = affectation;
        this.horloge = horloge;
    }

    /** EF3 — contrôles dans l'ordre du diagramme D3. */
    @Transactional
    public PresenceDto marquer(String code, Long etudiantId) {
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> Erreurs.etudiantInconnu(etudiantId));
        SessionCours session = sessions.findByCode(normaliser(code))
                .orElseThrow(Erreurs::codeInconnu);                                   // RG4
        if (!etudiant.appartientA(session.getPromotion())) {
            throw Erreurs.etudiantHorsPromotion();                                    // RG19
        }
        Instant maintenant = horloge.instant();
        if (!session.codeUtilisableA(maintenant)) {
            throw Erreurs.codeExpire();                                               // RG1, RG2
        }
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiantId)) {
            throw Erreurs.dejaPresent();                                              // RG3
        }
        Presence presence = presences.save(new Presence(session, etudiant, SourcePresence.ETUDIANT, maintenant));
        affectation.reaffecterEnAttente(session.getId());                             // RG9
        return PresenceDto.de(presence);
    }

    /** Le code est saisi au téléphone : on tolère espaces et minuscules. */
    static String normaliser(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
