package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;

import cm.kfokam48.presence.domain.StatutExercice;
import cm.kfokam48.presence.dto.ExerciceDeposeDto;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/**
 * Bug #32 — « deux étudiants tapent le code presque en même temps, un seul apparaît ».
 *
 * Reproduction déterministe : un exercice attend un relecteur, deux étudiants marquent leur présence
 * au même instant. Le générateur aléatoire de ce test fait attendre chaque transaction l'autre juste
 * avant l'insertion de la relecture : les deux ont alors lu l'exercice « en attente ».
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;"
        + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;LOCK_TIMEOUT=10000")
@DirtiesContext
class PresencesSimultaneesIntegrationTest {

    @TestConfiguration
    static class Configuration {

        @Bean
        @Primary
        RandomGenerator aleatoireSynchronise() {
            return new GenerateurQuiAttendLAutreTransaction();
        }
    }

    /** Chaque tirage attend (au plus 500 ms) que l'autre transaction en soit au même point. */
    static class GenerateurQuiAttendLAutreTransaction implements RandomGenerator {

        private final CyclicBarrier barriere = new CyclicBarrier(2);

        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public int nextInt(int borne) {
            try {
                barriere.await(500, TimeUnit.MILLISECONDS);
            } catch (Exception attenteInterrompueOuExpiree) {
                // l'autre transaction ne vient pas (ou plus) : on tire quand même
            }
            return 0;
        }
    }

    @Autowired
    SessionService sessions;

    @Autowired
    PresenceService presences;

    @Autowired
    ExerciceService exercices;

    @Autowired
    PresenceRepository presenceRepository;

    @Autowired
    RelectureRepository relectureRepository;

    @Test
    void deuxPresencesSimultanees_lesDeuxSontEnregistrees() throws Exception {
        var session = sessions.ouvrir("Bug #32", 2L);
        presences.marquer(session.code(), 7L);
        ExerciceDeposeDto exercice = exercices.deposer(session.id(), 7L, "https://github.com/dikoume/bug-32");
        assertThat(exercice.statut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTEUR);

        ExecutorService deuxTelephones = Executors.newFixedThreadPool(2);
        List<Callable<Object>> saisies = List.of(
                () -> presences.marquer(session.code(), 8L),
                () -> presences.marquer(session.code(), 9L));
        List<Future<Object>> resultats = deuxTelephones.invokeAll(saisies, 30, TimeUnit.SECONDS);
        deuxTelephones.shutdown();

        for (Future<Object> resultat : resultats) {
            resultat.get(); // relance l'exception si une des deux présences a échoué
        }
        assertThat(presenceRepository.findBySessionId(session.id())).hasSize(3);
        // RG7 v2 : les deux arrivants deviennent ses deux relecteurs, sans doublon ni perte de présence
        assertThat(relectureRepository.findByExerciceIdOrderByIdAsc(exercice.id()))
                .extracting(r -> r.getRelecteur().getId())
                .containsExactlyInAnyOrder(8L, 9L);
    }
}
