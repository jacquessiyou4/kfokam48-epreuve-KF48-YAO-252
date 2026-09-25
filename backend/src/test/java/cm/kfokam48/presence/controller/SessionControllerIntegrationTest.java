package cm.kfokam48.presence.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/** EF1 — contrat de POST /api/sessions et GET /api/sessions. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class SessionControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void ouvrirUneSession_renvoie201AvecUnCodeValableQuinzeMinutes() throws Exception {
        String corps = mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Tests d'intégration\",\"promotionId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code", matchesPattern("[A-HJKMNP-Z2-9]{6}")))
                .andReturn().getResponse().getContentAsString();

        Instant ouverture = Instant.parse(JsonPath.read(corps, "$.ouvertureAt"));
        Instant expiration = Instant.parse(JsonPath.read(corps, "$.expirationAt"));
        org.assertj.core.api.Assertions.assertThat(Duration.between(ouverture, expiration))
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void titreManquant_renvoie400Validation() throws Exception {
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promotionId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void promotionManquante_renvoie400Validation() throws Exception {
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void promotionInconnue_renvoie404() throws Exception {
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours\",\"promotionId\":999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    @Test
    void listerLesSessionsDeLaPromotionDeDemo() throws Exception {
        mvc.perform(get("/api/sessions").param("promotionId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.titre == 'Bases SQL')].cloturee").value(false))
                .andExpect(jsonPath("$[*].promotionId", everyItem(is(2))));
    }
}
