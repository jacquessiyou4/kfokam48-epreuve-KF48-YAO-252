package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Etudiant;
import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF6 — le système désigne les deux relecteurs d'un exercice (RG7 v2). */
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
     * RG7 v2, RG8 : tirage au hasard, sans remise, des relecteurs manquants parmi les présents
     * de la session — ni l'auteur (RG6), ni un étudiant déjà relecteur de cet exercice.
     * RG9 : faute d'éligibles, l'exercice attend la prochaine présence.
     */
    @Transactional
    public void affecter(Exercice exercice) {
        List<Relecture> existantes = relectures.findByExerciceIdOrderByIdAsc(exercice.getId());
        int manquants = exercice.getRelecteursRequis() - existantes.size();
        if (manquants <= 0) {
            return; // bug #32 : une transaction concurrente l'a déjà complété
        }
        Set<Long> exclus = new HashSet<>();
        exclus.add(exercice.getEtudiant().getId());
        existantes.forEach(r -> exclus.add(r.getRelecteur().getId()));
        List<Etudiant> eligibles = new ArrayList<>(presences.findBySessionId(exercice.getSession().getId()).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !exclus.contains(e.getId()))
                .toList());
        for (int i = 0; i < manquants && !eligibles.isEmpty(); i++) {
            Etudiant relecteur = eligibles.remove(aleatoire.nextInt(eligibles.size()));
            relectures.save(new Relecture(exercice, relecteur, horloge.instant()));
            exercice.marquerRelecteurAffecte();
        }
    }

    /**
     * RG9 : appelé à chaque nouvelle présence dans la session (transitions T5 et T8 de D4).
     * Les exercices à compléter sont verrouillés : deux présences simultanées ne peuvent plus
     * les affecter toutes les deux (bug #32).
     */
    @Transactional
    public void reaffecterEnAttente(Long sessionId) {
        exercices.verrouillerSansAssezDeRelecteurs(sessionId).forEach(this::affecter);
    }
}
