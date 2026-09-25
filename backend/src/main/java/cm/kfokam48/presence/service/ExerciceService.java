package cm.kfokam48.presence.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.SessionCours;
import cm.kfokam48.presence.dto.ExerciceDeposeDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

@Service
public class ExerciceService {

    private final ExerciceRepository exercices;
    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final AffectationService affectation;
    private final Clock horloge;

    public ExerciceService(ExerciceRepository exercices, SessionCoursRepository sessions,
            EtudiantRepository etudiants, AffectationService affectation, Clock horloge) {
        this.exercices = exercices;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.affectation = affectation;
        this.horloge = horloge;
    }

    /** EF5 + EF6 — le relecteur est affecté dans la même transaction que le dépôt (D4, T1 à T4). */
    @Transactional
    public ExerciceDeposeDto deposer(Long sessionId, Long etudiantId, String lien) {
        if (!ValidateurLien.estValide(lien)) {
            throw Erreurs.lienInvalide();                                             // RG18
        }
        SessionCours session = sessions.findById(sessionId)
                .orElseThrow(() -> Erreurs.sessionInconnue(sessionId));
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> Erreurs.etudiantInconnu(etudiantId));
        if (!etudiant.appartientA(session.getPromotion())) {
            throw Erreurs.etudiantHorsPromotion();                                    // RG19
        }
        if (session.estCloturee()) {
            throw Erreurs.sessionCloturee();                                          // RG13
        }
        if (exercices.existsBySessionIdAndEtudiantId(sessionId, etudiantId)) {
            throw Erreurs.exerciceDejaDepose();                                       // RG12
        }
        // H7 : un étudiant absent peut déposer ; il ne sera simplement pas relecteur de cette session.
        Exercice exercice = exercices.save(new Exercice(session, etudiant, lien.trim(), horloge.instant()));
        affectation.affecter(exercice);
        return ExerciceDeposeDto.de(exercice);
    }
}
