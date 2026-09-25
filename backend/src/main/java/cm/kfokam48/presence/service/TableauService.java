package cm.kfokam48.presence.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Presence;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.domain.SourcePresence;
import cm.kfokam48.presence.domain.StatutExercice;
import cm.kfokam48.presence.dto.LigneTableauDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/**
 * EF9 — tableau du formateur. Nombre de requêtes fixe quelle que soit la taille de la promotion (ENF2) :
 * on charge présences, exercices et relectures de la promotion, puis on agrège en mémoire.
 */
@Service
@Transactional(readOnly = true)
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(PromotionRepository promotions, EtudiantRepository etudiants, PresenceRepository presences,
            ExerciceRepository exercices, RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    public List<LigneTableauDto> tableau(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw Erreurs.promotionInconnue(promotionId);
        }
        List<Presence> presencesPromo = presences.findBySessionPromotionId(promotionId);
        Map<Long, Long> presencesParEtudiant = presencesPromo.stream()
                .collect(Collectors.groupingBy(p -> p.getEtudiant().getId(), Collectors.counting()));
        Map<Long, Long> ajouteesParFormateur = presencesPromo.stream()
                .filter(p -> p.getSource() == SourcePresence.FORMATEUR)
                .collect(Collectors.groupingBy(p -> p.getEtudiant().getId(), Collectors.counting()));
        List<Exercice> exercicesPromo = exercices.findBySessionPromotionId(promotionId);
        List<Relecture> relecturesPromo = relectures.findAvecExerciceParPromotion(promotionId);
        Map<Long, List<Relecture>> relecturesParExercice = relecturesPromo.stream()
                .collect(Collectors.groupingBy(r -> r.getExercice().getId()));

        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream().map(etudiant -> {
            Long id = etudiant.getId();
            List<Exercice> siens = exercicesPromo.stream().filter(e -> e.getEtudiant().getId().equals(id)).toList();
            // RG17 v2 : moyenne des notes retenues (RG22), provisoire si l'une l'est (RG23)
            List<NoteRetenue> notesRetenues = siens.stream()
                    .map(e -> NoteRetenue.de(e, relecturesParExercice.getOrDefault(e.getId(), List.of())))
                    .filter(Objects::nonNull)
                    .toList();
            int relecturesEnAttente = (int) relecturesPromo.stream()
                    .filter(r -> !r.estRendue() && r.getRelecteur().getId().equals(id))
                    .count();
            int exercicesEnAttente = (int) siens.stream().filter(e -> e.getStatut() != StatutExercice.RELU).count();
            return new LigneTableauDto(id, etudiant.getNom(), presencesParEtudiant.getOrDefault(id, 0L).intValue(),
                    siens.size(), Moyenne.deNotesRetenues(notesRetenues), relecturesEnAttente, exercicesEnAttente,
                    notesRetenues.stream().anyMatch(NoteRetenue::provisoire),
                    ajouteesParFormateur.getOrDefault(id, 0L).intValue());
        }).toList();
    }
}
