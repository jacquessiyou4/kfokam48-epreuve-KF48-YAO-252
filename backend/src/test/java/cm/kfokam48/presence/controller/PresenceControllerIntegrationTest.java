package cm.kfokam48.presence.controller;

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

/** EF3 — contrat de POST /api/presences, chaque branche de D3. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class PresenceControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    private String ouvrirSession() throws Exception {
        String corps = mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Présences\",\"promotionId\":1}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corps, "$.code");
    }

    private ResultActions marquer(String code, long etudiantId) throws Exception {
        return mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    void codeValide_puisDeuxiemeTentative() throws Exception {
        String code = ouvrirSession();

        marquer(code, 5).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(5))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
        marquer(code, 5).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    void codeInconnu_renvoie400() throws Exception {
        marquer("ZZZZZZ", 1).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
    }

    @Test
    void codeExpire_renvoie410() throws Exception {
        // Session 2 des données de démo : ouverte hier, non clôturée
        marquer("HSTC3D", 5).andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void sessionCloturee_renvoie410() throws Exception {
        marquer("HSTA2B", 6).andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void etudiantDUneAutrePromotion_renvoie403() throws Exception {
        marquer(ouvrirSession(), 7).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
    }

    @Test
    void etudiantInconnu_renvoie404() throws Exception {
        marquer(ouvrirSession(), 999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }

    @Test
    void champManquant_renvoie400Validation() throws Exception {
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"ABCDEF\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
