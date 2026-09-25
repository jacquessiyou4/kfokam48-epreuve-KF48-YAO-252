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

import com.jayway.jsonpath.JsonPath;

/** RG5 — le 6e essai est refusé en 429, même avec le bon code (D3, branche 3). */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class BlocageTentativesIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void cinqCodesErronesPuisLeBonCode_renvoie429() throws Exception {
        String code = JsonPath.read(mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"RG5\",\"promotionId\":1}")).andReturn().getResponse().getContentAsString(), "$.code");
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ZZZZZZ\",\"etudiantId\":4}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CODE_INCONNU"));
        }
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":4}"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.code").value("TROP_DE_TENTATIVES"));
        // Un autre étudiant n'est pas concerné (H10)
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":3}"))
                .andExpect(status().isCreated());
    }
}
