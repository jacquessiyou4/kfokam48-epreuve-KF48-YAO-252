package cm.kfokam48.presence.config;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Générateur aléatoire injectable : les tests du tirage du relecteur (RG8) sont déterministes. */
@Configuration
public class AleatoireConfig {

    @Bean
    public RandomGenerator aleatoire() {
        return new SecureRandom();
    }
}
