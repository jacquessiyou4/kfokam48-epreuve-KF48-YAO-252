package cm.kfokam48.presence.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.RelectureAFaireDto;
import cm.kfokam48.presence.dto.RendreRelectureRequete;
import cm.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    @PostMapping("/{id}")
    public RelectureAFaireDto rendre(@PathVariable Long id, @Valid @RequestBody RendreRelectureRequete requete) {
        return service.rendre(id, requete.note(), requete.commentaire(), requete.relecteurId());
    }
}
