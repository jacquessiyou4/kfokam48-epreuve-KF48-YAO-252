package cm.kfokam48.presence.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** EF7 — relectures assignées à un étudiant. */
@SpringBootTest
@AutoConfigureMockMvc
class EtudiantControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void relectureDeLEtudiant2_nonRenduesDAbord_sansNomDAuteur() throws Exception {
        // Démo : l'étudiant 2 a rendu la relecture 1 et doit encore faire la relecture 7
        mvc.perform(get("/api/etudiants/2/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].rendue").value(false))
                .andExpect(jsonPath("$[0].lien").value("https://github.com/mbarga/api-rest"))
                .andExpect(jsonPath("$[*].id", hasItem(1)))
                .andExpect(content().string(not(containsString("MBARGA"))));
    }

    @Test
    void etudiantInconnu_renvoie404() throws Exception {
        mvc.perform(get("/api/etudiants/999/relectures"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}
