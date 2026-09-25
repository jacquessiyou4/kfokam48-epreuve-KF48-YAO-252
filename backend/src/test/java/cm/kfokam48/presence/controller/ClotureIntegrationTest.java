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

import com.jayway.jsonpath.JsonPath;

/** EF10 — la clôture fige la session : RG2 (code), RG13 (dépôt), RG20 (relecture). */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class ClotureIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void clotureDUneSessionOuverte_puisToutEstFige() throws Exception {
        String code = JsonPath.read(mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"À clôturer\",\"promotionId\":1}")).andReturn().getResponse().getContentAsString(), "$.code");
        String presence = mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":1}")).andReturn().getResponse().getContentAsString();
        long sessionId = ((Number) JsonPath.read(presence, "$.sessionId")).longValue();

        mvc.perform(post("/api/sessions/" + sessionId + "/cloture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cloturee").value(true))
                .andExpect(jsonPath("$.clotureeAt").isNotEmpty());

        // RG2 : le code ne marche plus, même dans les 15 minutes
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":2}"))
                .andExpect(status().isGone()).andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
        // RG13 : plus de dépôt
        mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":1,\"lien\":\"https://x.cm\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
        // Seconde clôture
        mvc.perform(post("/api/sessions/" + sessionId + "/cloture"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"));
    }

    @Test
    void q11_lesRelecturesNonRenduesRestentVisiblesApresCloture() throws Exception {
        // Session 2 de démo : relecture 7 (relecteur 2) non rendue
        mvc.perform(post("/api/sessions/2/cloture")).andExpect(status().isOk());
        mvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(jsonPath("$[?(@.etudiantId == 2)].relecturesEnAttente").value(1));
        // RG20 : elle ne peut plus être rendue
        mvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":10,\"commentaire\":\"Trop tard\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void sessionInconnue_renvoie404() throws Exception {
        mvc.perform(post("/api/sessions/999/cloture"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }
}
