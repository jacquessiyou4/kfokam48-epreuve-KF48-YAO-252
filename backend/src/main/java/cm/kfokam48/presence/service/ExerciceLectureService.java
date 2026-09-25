package cm.kfokam48.presence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.dto.ExerciceDeLEtudiantDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF11 v2 — l'étudiant voit la note retenue de ses exercices, provisoire ou non (RG22, RG23). */
@Service
@Transactional(readOnly = true)
public class ExerciceLectureService {

    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;

    public ExerciceLectureService(ExerciceRepository exercices, RelectureRepository relectures,
            EtudiantRepository etudiants) {
        this.exercices = exercices;
        this.relectures = relectures;
        this.etudiants = etudiants;
    }

    public List<ExerciceDeLEtudiantDto> mesExercices(Long etudiantId) {
        if (!etudiants.existsById(etudiantId)) {
            throw Erreurs.etudiantInconnu(etudiantId);
        }
        return exercices.findByEtudiantIdOrderByDeposeAtDesc(etudiantId).stream().map(this::versDto).toList();
    }

    private ExerciceDeLEtudiantDto versDto(Exercice e) {
        List<Relecture> siennes = relectures.findByExerciceIdOrderByIdAsc(e.getId());
        NoteRetenue note = NoteRetenue.de(e, siennes);
        // RG16 : seuls les commentaires sortent, jamais le relecteur
        List<String> commentaires = siennes.stream().filter(Relecture::estRendue).map(Relecture::getCommentaire).toList();
        return new ExerciceDeLEtudiantDto(e.getId(), e.getSession().getId(), e.getSession().getTitre(), e.getLien(),
                e.getStatut(), note == null ? null : note.valeur(), note != null && note.provisoire(), commentaires);
    }
}
