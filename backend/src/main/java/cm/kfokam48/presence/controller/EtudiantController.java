package cm.kfokam48.presence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.ExerciceDeLEtudiantDto;
import cm.kfokam48.presence.dto.RelectureAFaireDto;
import cm.kfokam48.presence.service.ExerciceLectureService;
import cm.kfokam48.presence.service.RelectureService;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final RelectureService relectures;
    private final ExerciceLectureService exercices;

    public EtudiantController(RelectureService relectures, ExerciceLectureService exercices) {
        this.relectures = relectures;
        this.exercices = exercices;
    }

    @GetMapping("/{id}/exercices")
    public List<ExerciceDeLEtudiantDto> exercices(@PathVariable Long id) {
        return exercices.mesExercices(id);
    }

    @GetMapping("/{id}/relectures")
    public List<RelectureAFaireDto> relectures(@PathVariable Long id) {
        return relectures.aFaire(id);
    }
}
