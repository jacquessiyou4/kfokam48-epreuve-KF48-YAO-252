package cm.kfokam48.presence.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** EF2 — listes de promotions et d'étudiants. */
@SpringBootTest
@AutoConfigureMockMvc
class PromotionControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void promotionsTrieesParNom() throws Exception {
        mvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Promotion Douala 2026"))
                .andExpect(jsonPath("$[1].nom").value("Promotion Yaoundé 2026"));
    }

    @Test
    void etudiantsDUnePromotionTriesParNom() throws Exception {
        mvc.perform(get("/api/promotions/1/etudiants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].nom").value("AMOUGOU Brice"))
                .andExpect(jsonPath("$[0].promotionId").value(1));
    }

    @Test
    void promotionInconnue_renvoie404() throws Exception {
        mvc.perform(get("/api/promotions/999/etudiants"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
