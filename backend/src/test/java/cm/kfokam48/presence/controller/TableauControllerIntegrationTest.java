package cm.kfokam48.presence.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManagerFactory;

/** EF9 — GET /api/tableau sur les données de démo (valeurs calculées à la main depuis V2). */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class TableauControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EntityManagerFactory emf;

    @Test
    void tableauDeLaPromotion1() throws Exception {
        mvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                // AMOUGOU : 2 présences, 2 exercices (15/20 reçu, 1 en attente), aucune relecture due
                .andExpect(jsonPath("$[0].nom").value("AMOUGOU Brice"))
                .andExpect(jsonPath("$[0].presences").value(2))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(2))
                .andExpect(jsonPath("$[0].moyenne").value(15.0))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(0))
                .andExpect(jsonPath("$[0].exercicesEnAttente").value(1))
                .andExpect(jsonPath("$[0].moyenneProvisoire").value(false))
                // BELINGA : doit encore la relecture 7
                .andExpect(jsonPath("$[1].moyenne").value(12.0))
                .andExpect(jsonPath("$[1].relecturesEnAttente").value(1))
                // FOTSO : exercice jamais relu (Q11) → pas de moyenne, bien visible en attente
                .andExpect(jsonPath("$[3].nom").value("FOTSO Mireille"))
                .andExpect(jsonPath("$[3].moyenne").doesNotExist())
                .andExpect(jsonPath("$[3].exercicesEnAttente").value(1))
                // MBARGA : présence ajoutée par le formateur, relecture 4 jamais rendue
                .andExpect(jsonPath("$[4].presences").value(1))
                .andExpect(jsonPath("$[4].relecturesEnAttente").value(1));
    }

    @Test
    void enf2_nombreDeRequetesIndependantDuNombreDEtudiants() throws Exception {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        mvc.perform(get("/api/tableau").param("promotionId", "1")).andExpect(status().isOk());

        // existence de la promotion + étudiants + présences + exercices + relectures
        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(5);
    }

    @Test
    void promotionInconnue_404_etParametreManquant_400() throws Exception {
        mvc.perform(get("/api/tableau").param("promotionId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
        mvc.perform(get("/api/tableau"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
