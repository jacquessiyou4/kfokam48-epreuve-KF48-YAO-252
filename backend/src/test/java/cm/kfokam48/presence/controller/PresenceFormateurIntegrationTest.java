package cm.kfokam48.presence.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

/** EF4 — présence ajoutée par le formateur (Q14, RG15). */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class PresenceFormateurIntegrationTest {

    @Autowired
    MockMvc mvc;

    private ResultActions ajouter(long sessionId, long etudiantId) throws Exception {
        return mvc.perform(post("/api/sessions/" + sessionId + "/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    void codeExpire_maisLeFormateurPeutAjouterLaPresence() throws Exception {
        // Session 2 de démo : code expiré depuis hier, non clôturée ; l'étudiant 5 était absent
        ajouter(2, 5).andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"))
                .andExpect(jsonPath("$.etudiantId").value(5));
        mvc.perform(get("/api/tableau").param("promotionId", "1"))
                // MBARGA : présence manuelle de la démo (session 1) + celle-ci
                .andExpect(jsonPath("$[?(@.etudiantId == 5)].presencesAjouteesParFormateur").value(2));
        ajouter(2, 5).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    void erreurs() throws Exception {
        ajouter(1, 6).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
        ajouter(2, 7).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
        ajouter(999, 1).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
        mvc.perform(post("/api/sessions/2/presences").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
