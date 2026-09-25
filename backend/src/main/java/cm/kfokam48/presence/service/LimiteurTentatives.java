package cm.kfokam48.presence.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cm.kfokam48.presence.erreur.Erreurs;

/**
 * RG5 (Q4) — après 5 codes inconnus consécutifs, l'étudiant est bloqué 2 minutes.
 * Le blocage porte sur l'étudiant, pas sur l'adresse IP (H10) ; il est gardé en mémoire,
 * donc perdu au redémarrage — accepté dans le cahier des charges.
 */
@Component
public class LimiteurTentatives {

    static final int ECHECS_MAX = 5;
    static final Duration DUREE_BLOCAGE = Duration.ofMinutes(2);

    private record Etat(int echecs, Instant bloqueJusqua) {
    }

    private final Map<Long, Etat> etats = new ConcurrentHashMap<>();
    private final Clock horloge;

    public LimiteurTentatives(Clock horloge) {
        this.horloge = horloge;
    }

    public void verifierNonBloque(Long etudiantId) {
        Etat etat = etats.get(etudiantId);
        if (etat != null && etat.bloqueJusqua() != null && horloge.instant().isBefore(etat.bloqueJusqua())) {
            throw Erreurs.tropDeTentatives();
        }
    }

    public void enregistrerEchec(Long etudiantId) {
        etats.compute(etudiantId, (id, etat) -> {
            int echecs = (etat == null || etat.bloqueJusqua() != null ? 0 : etat.echecs()) + 1;
            return echecs >= ECHECS_MAX
                    ? new Etat(0, horloge.instant().plus(DUREE_BLOCAGE))
                    : new Etat(echecs, null);
        });
    }

    public void reinitialiser(Long etudiantId) {
        etats.remove(etudiantId);
    }
}
