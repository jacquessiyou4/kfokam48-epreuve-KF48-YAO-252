package cm.kfokam48.presence.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Changement de besoin de l'étape 3, de bout en bout : deux relecteurs, note provisoire tant qu'un seul
 * a rendu, puis moyenne définitive (EF8, EF9, EF11 v2 — RG22, RG23).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class DoubleRelectureIntegrationTest {

    @Autowired
    MockMvc mvc;

    private String envoyer(String url, String corps) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(corps)).andReturn().getResponse().getContentAsString();
    }

    private long relectureAFaire(long relecteurId, long exerciceId) throws Exception {
        String liste = mvc.perform(get("/api/etudiants/" + relecteurId + "/relectures")).andReturn().getResponse()
                .getContentAsString();
        List<Number> ids = JsonPath.read(liste, "$[?(@.exerciceId == " + exerciceId + ")].id");
        return ids.get(0).longValue();
    }

    @Test
    void noteProvisoirePuisMoyenneDefinitive() throws Exception {
        String code = JsonPath.read(envoyer("/api/sessions", "{\"titre\":\"Double relecture\",\"promotionId\":1}"), "$.code");
        for (long etudiant : new long[] { 4, 5, 6 }) {
            envoyer("/api/presences", "{\"code\":\"" + code + "\",\"etudiantId\":" + etudiant + "}");
        }
        long sessionId = ((Number) JsonPath.read(envoyer("/api/presences", "{\"code\":\"" + code + "\",\"etudiantId\":3}"),
                "$.sessionId")).longValue();
        // Seuls 3, 4, 5, 6 sont présents ; l'auteur 3 est exclu : deux relecteurs parmi 4, 5, 6
        long exerciceId = ((Number) JsonPath.read(envoyer("/api/exercices",
                "{\"sessionId\":" + sessionId + ",\"etudiantId\":3,\"lien\":\"https://github.com/essomba/double\"}"),
                "$.id")).longValue();

        List<Integer> relecteurs = new java.util.ArrayList<>();
        for (int etudiant : new int[] { 4, 5, 6 }) {
            String liste = mvc.perform(get("/api/etudiants/" + etudiant + "/relectures")).andReturn().getResponse()
                    .getContentAsString();
            if (!((List<?>) JsonPath.read(liste, "$[?(@.exerciceId == " + exerciceId + ")]")).isEmpty()) {
                relecteurs.add(etudiant);
            }
        }
        org.assertj.core.api.Assertions.assertThat(relecteurs).hasSize(2);

        // Premier relecteur : note provisoire
        mvc.perform(post("/api/relectures/" + relectureAFaire(relecteurs.get(0), exerciceId))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":12,\"commentaire\":\"Premier avis\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/etudiants/3/exercices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(exerciceId))
                .andExpect(jsonPath("$[0].statut").value("RELU_PARTIELLEMENT"))
                .andExpect(jsonPath("$[0].noteRetenue").value(12.0))
                .andExpect(jsonPath("$[0].noteProvisoire").value(true))
                .andExpect(jsonPath("$[0].commentaires", contains("Premier avis")))
                .andExpect(content().string(not(containsString("relecteur"))));
        mvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(jsonPath("$[?(@.etudiantId == 3)].moyenneProvisoire").value(true))
                // ESSOMBA : 18 (démo, définitive) et 12 (provisoire) → 15.00
                .andExpect(jsonPath("$[?(@.etudiantId == 3)].moyenne").value(15.0));

        // Second relecteur : moyenne des deux, définitive
        mvc.perform(post("/api/relectures/" + relectureAFaire(relecteurs.get(1), exerciceId))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":15,\"commentaire\":\"Second avis\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/etudiants/3/exercices"))
                .andExpect(jsonPath("$[0].statut").value("RELU"))
                .andExpect(jsonPath("$[0].noteRetenue").value(13.5))
                .andExpect(jsonPath("$[0].noteProvisoire").value(false))
                .andExpect(jsonPath("$[0].commentaires", contains("Premier avis", "Second avis")));
        mvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(jsonPath("$[?(@.etudiantId == 3)].moyenneProvisoire").value(false))
                // (18 + 13.5) / 2 = 15.75
                .andExpect(jsonPath("$[?(@.etudiantId == 3)].moyenne").value(15.75));
    }

    @Test
    void etudiantInconnu_renvoie404() throws Exception {
        mvc.perform(get("/api/etudiants/999/exercices"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}
