package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.List;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.domain.StatutExercice;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF6 — le système désigne le relecteur d'un exercice. */
@Service
public class AffectationService {

    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final RandomGenerator aleatoire;
    private final Clock horloge;

    public AffectationService(PresenceRepository presences, ExerciceRepository exercices,
            RelectureRepository relectures, RandomGenerator aleatoire, Clock horloge) {
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
        this.aleatoire = aleatoire;
        this.horloge = horloge;
    }

    /**
     * RG8 : tirage au hasard parmi les présents de la session, auteur exclu (RG6) ; un seul relecteur (RG7).
     * RG9 : sans relecteur éligible, l'exercice reste EN_ATTENTE_RELECTEUR.
     */
    @Transactional
    public void affecter(Exercice exercice) {
        if (relectures.existsByExerciceId(exercice.getId())) {
            return; // bug #32 : une transaction concurrente vient de l'affecter (RG7)
        }
        Long auteurId = exercice.getEtudiant().getId();
        List<Etudiant> eligibles = presences.findBySessionId(exercice.getSession().getId()).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !e.getId().equals(auteurId))
                .toList();
        if (eligibles.isEmpty()) {
            return;
        }
        Etudiant relecteur = eligibles.get(aleatoire.nextInt(eligibles.size()));
        relectures.save(new Relecture(exercice, relecteur, horloge.instant()));
        exercice.marquerRelecteurAffecte();
    }

    /**
     * RG9 : appelé à chaque nouvelle présence dans la session (transition T5 de D4).
     * Les exercices en attente sont verrouillés : deux présences simultanées ne peuvent plus
     * les affecter toutes les deux (bug #32).
     */
    @Transactional
    public void reaffecterEnAttente(Long sessionId) {
        exercices.verrouillerParSessionEtStatut(sessionId, StatutExercice.EN_ATTENTE_RELECTEUR)
                .forEach(this::affecter);
    }
}
