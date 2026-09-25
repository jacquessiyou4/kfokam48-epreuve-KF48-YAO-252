package cm.kfokam48.presence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.EtudiantDto;
import cm.kfokam48.presence.dto.PromotionDto;
import cm.kfokam48.presence.erreur.Erreurs;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;

/** EF2 — l'étudiant se choisit dans la liste, sans mot de passe (Q1). */
@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public PromotionService(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    public List<PromotionDto> lister() {
        return promotions.findAllByOrderByNomAsc().stream().map(PromotionDto::de).toList();
    }

    public List<EtudiantDto> etudiants(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw Erreurs.promotionInconnue(promotionId);
        }
        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream().map(EtudiantDto::de).toList();
    }
}
