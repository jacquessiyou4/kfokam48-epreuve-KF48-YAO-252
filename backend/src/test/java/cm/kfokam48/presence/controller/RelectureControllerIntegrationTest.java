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

/** EF8 — contrat de POST /api/relectures/{id}, sur les données de démo. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class RelectureControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    private ResultActions rendre(long id, String corps) throws Exception {
        return mvc.perform(post("/api/relectures/" + id).contentType(MediaType.APPLICATION_JSON).content(corps));
    }

    @Test
    void relecture5_erreursPuisRenduePuisDefinitive() throws Exception {
        // Démo : relecture 5 = exercice de l'étudiant 1 (session 2 ouverte), relecteur 6
        rendre(5, "{\"note\":21,\"commentaire\":\"x\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
        rendre(5, "{\"note\":12.5,\"commentaire\":\"x\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
        rendre(5, "{\"note\":12,\"commentaire\":\"x\",\"relecteurId\":1}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
        rendre(5, "{\"note\":12,\"commentaire\":\"x\",\"relecteurId\":3}").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RELECTEUR_NON_ASSIGNE"));

        rendre(5, "{\"note\":16,\"commentaire\":\"Bonne gestion des erreurs.\",\"relecteurId\":6}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rendue").value(true))
                .andExpect(jsonPath("$.note").value(16));

        rendre(5, "{\"note\":8,\"commentaire\":\"Je change d'avis\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    void corpsImposeSansRelecteurId_estAccepte() throws Exception {
        // Relecture 7 : exercice de l'étudiant 5, relecteur 2, session 2 ouverte
        rendre(7, "{\"note\":0,\"commentaire\":\"Lien vide.\"}").andExpect(status().isOk());
    }

    @Test
    void sessionCloturee_renvoie409() throws Exception {
        // Relecture 4 : session 1 clôturée, jamais rendue
        rendre(4, "{\"note\":10,\"commentaire\":\"Trop tard\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void relectureInconnue_404_etCommentaireManquant_400() throws Exception {
        rendre(999, "{\"note\":10,\"commentaire\":\"x\"}").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
        rendre(5, "{\"note\":10}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
