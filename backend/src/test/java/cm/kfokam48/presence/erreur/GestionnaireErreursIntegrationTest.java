package cm.kfokam48.presence.erreur;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/** #2 — toute erreur sort au format { code, message }, sans stack trace (B4, ENF3, ENF4). */
@SpringBootTest
@AutoConfigureMockMvc
@Import(GestionnaireErreursIntegrationTest.ControleurDeTest.class)
class GestionnaireErreursIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void adresseInconnue_renvoie404AuFormatImpose() throws Exception {
        mvc.perform(get("/api/nexiste-pas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void jsonMalForme_renvoie400Validation() throws Exception {
        mvc.perform(post("/test/valider").contentType(MediaType.APPLICATION_JSON).content("{ pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void champObligatoireManquant_renvoie400ValidationAvecLeChamp() throws Exception {
        mvc.perform(post("/test/valider").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message", containsString("titre")));
    }

    @Test
    void exceptionInattendue_renvoie500SansStackTrace() throws Exception {
        mvc.perform(get("/test/panne"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("at cm.kfokam48"))));
    }

    @Test
    void erreurMetier_renvoieSonStatutEtSonCode() throws Exception {
        mvc.perform(get("/test/metier"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
                .andExpect(jsonPath("$.message").value("Le code de présence a expiré."));
    }

    record CorpsDeTest(@NotBlank String titre) {
    }

    @TestConfiguration
    @RestController
    static class ControleurDeTest {

        @PostMapping("/test/valider")
        String valider(@Valid @RequestBody CorpsDeTest corps) {
            return "ok";
        }

        @GetMapping("/test/panne")
        String panne() {
            throw new IllegalStateException("détail interne qui ne doit pas fuiter");
        }

        @GetMapping("/test/metier")
        String metier() {
            throw new ErreurMetierException(org.springframework.http.HttpStatus.GONE,
                    "CODE_EXPIRE", "Le code de présence a expiré.");
        }
    }
}
