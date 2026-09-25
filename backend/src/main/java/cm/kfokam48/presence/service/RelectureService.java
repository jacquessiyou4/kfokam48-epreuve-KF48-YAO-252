package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.domain.Exercice;
import cm.kfokam48.presence.domain.Relecture;
import cm.kfokam48.presence.dto.RelectureAFaireDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

@Service
public class RelectureService {

    static final int NOTE_MIN = 0;
    static final int NOTE_MAX = 20;

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants, Clock horloge) {
        this.relectures = relectures;
        this.etudiants = etudiants;
        this.horloge = horloge;
    }

    /** EF7 — les relectures assignées, les non rendues d'abord. */
    @Transactional(readOnly = true)
    public List<RelectureAFaireDto> aFaire(Long etudiantId) {
        if (!etudiants.existsById(etudiantId)) {
            throw Erreurs.etudiantInconnu(etudiantId);
        }
        return relectures.findByRelecteurIdOrderByRendueAtAscAssigneeAtAsc(etudiantId).stream()
                .map(RelectureAFaireDto::de)
                .sorted(Comparator.comparing(RelectureAFaireDto::rendue))
                .toList();
    }

    /** EF8 — contrôles dans l'ordre du contrat : 404 → 400 → 403 → 409. */
    @Transactional
    public RelectureAFaireDto rendre(Long relectureId, BigDecimal note, String commentaire, Long relecteurId) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> Erreurs.relectureInconnue(relectureId));
        int noteEntiere = noteValide(note);                                           // RG10
        Exercice exercice = relecture.getExercice();
        if (relecteurId != null) {                                                    // H1
            if (relecteurId.equals(exercice.getEtudiant().getId())) {
                throw Erreurs.autoRelecture();                                        // RG6
            }
            if (!relecteurId.equals(relecture.getRelecteur().getId())) {
                throw Erreurs.relecteurNonAssigne();
            }
        }
        if (relecture.estRendue()) {
            throw Erreurs.relectureDejaRendue();                                      // RG11
        }
        if (exercice.getSession().estCloturee()) {
            throw Erreurs.sessionCloturee();                                          // RG20
        }
        relecture.rendre(noteEntiere, commentaire.trim(), horloge.instant());
        exercice.marquerRelu();                                                       // D4, T7
        return RelectureAFaireDto.de(relecture);
    }

    /** RG10 — entier de 0 à 20 ; 12.5, 21, -1 ou une note absente sont refusés. */
    static int noteValide(BigDecimal note) {
        if (note == null) {
            throw Erreurs.noteInvalide();
        }
        BigDecimal normalisee = note.stripTrailingZeros();
        if (normalisee.scale() > 0
                || normalisee.compareTo(BigDecimal.valueOf(NOTE_MIN)) < 0
                || normalisee.compareTo(BigDecimal.valueOf(NOTE_MAX)) > 0) {
            throw Erreurs.noteInvalide();
        }
        return normalisee.intValueExact();
    }
}
