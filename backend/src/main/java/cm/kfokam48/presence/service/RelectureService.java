package cm.kfokam48.presence.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.RelectureAFaireDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;

    public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants) {
        this.relectures = relectures;
        this.etudiants = etudiants;
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
}
