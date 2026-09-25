package cm.kfokam48.presence.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import cm.kfokam48.presence.repository.RelectureRepository;

/** EF5, EF6 — contrat de POST /api/exercices et affectation du relecteur. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class ExerciceControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RelectureRepository relectures;

    private ResultActions deposer(long sessionId, long etudiantId, String lien) throws Exception {
        String champLien = lien == null ? "" : ",\"lien\":\"" + lien + "\"";
        return mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId + champLien + "}"));
    }

    @Test
    void depot_affecteUnRelecteurPresentPuisRefuseUnSecondDepot() throws Exception {
        // Session 2 de démo : présents 1, 2, 3, 4, 6 ; l'étudiant 2 n'a pas encore déposé
        String corps = deposer(2, 2, "https://github.com/belinga/api-rest")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"))
                .andReturn().getResponse().getContentAsString();

        Long exerciceId = ((Number) JsonPath.read(corps, "$.id")).longValue();
        // RG7 v2 : deux relecteurs différents, présents, jamais l'auteur
        var relecteurs = relectures.findByExerciceIdOrderByIdAsc(exerciceId).stream()
                .map(r -> r.getRelecteur().getId()).toList();
        assertThat(relecteurs).hasSize(2).doesNotHaveDuplicates().allMatch(id -> java.util.List.of(1L, 3L, 4L, 6L).contains(id));

        deposer(2, 2, "https://github.com/belinga/autre").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }

    @Test
    void lienInvalideOuAbsent_renvoie400() throws Exception {
        deposer(2, 3, "ftp://serveur/exo").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
        deposer(2, 3, "pas un lien").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
        deposer(2, 3, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void sessionCloturee_renvoie409() throws Exception {
        deposer(1, 6, "https://github.com/ngono/spring-intro").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void sessionInconnue_renvoie404_etAutrePromotion_renvoie403() throws Exception {
        deposer(999, 1, "https://x.cm").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
        deposer(2, 7, "https://x.cm").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
    }

    @Test
    void rg9_sansRelecteurEligible_puisAffectationALaPresenceSuivante() throws Exception {
        String code = JsonPath.read(mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"RG9\",\"promotionId\":2}")).andReturn().getResponse().getContentAsString(), "$.code");
        Long sessionId = ((Number) JsonPath.read(mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":7}")).andReturn().getResponse().getContentAsString(),
                "$.sessionId")).longValue();

        // Seul l'auteur (7) est présent : aucun relecteur éligible
        String corps = deposer(sessionId, 7, "https://github.com/dikoume/rg9")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTEUR"))
                .andReturn().getResponse().getContentAsString();
        Long exerciceId = ((Number) JsonPath.read(corps, "$.id")).longValue();
        assertThat(relectures.findByExerciceIdOrderByIdAsc(exerciceId)).isEmpty();

        // L'étudiant 8 arrive : l'affectation est retentée, il devient le premier relecteur
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":8}")).andExpect(status().isCreated());
        assertThat(relectures.findByExerciceIdOrderByIdAsc(exerciceId)).extracting(r -> r.getRelecteur().getId())
                .containsExactly(8L);

        // L'étudiant 9 arrive : le second relecteur manquant est affecté (RG9 v2, T8 de D4)
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":9}")).andExpect(status().isCreated());
        assertThat(relectures.findByExerciceIdOrderByIdAsc(exerciceId)).extracting(r -> r.getRelecteur().getId())
                .containsExactly(8L, 9L);
    }

    @Test
    void h7_unEtudiantAbsentPeutDeposer() throws Exception {
        // L'étudiant 5 était absent en session 2 (et a déjà déposé) ; l'étudiant 9 est absent en session 3
        deposer(3, 9, "https://github.com/njoh/bases-sql").andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));
    }
}
