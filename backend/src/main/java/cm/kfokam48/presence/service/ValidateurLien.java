package cm.kfokam48.presence.service;

import java.net.URI;
import java.net.URISyntaxException;

/** RG18 — un lien valide est une URL absolue http(s) de moins de 500 caractères. */
final class ValidateurLien {

    static final int LONGUEUR_MAX = 500;

    private ValidateurLien() {
    }

    static boolean estValide(String lien) {
        if (lien == null || lien.isBlank() || lien.length() > LONGUEUR_MAX) {
            return false;
        }
        try {
            URI uri = new URI(lien.trim());
            String schema = uri.getScheme();
            return ("http".equalsIgnoreCase(schema) || "https".equalsIgnoreCase(schema))
                    && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
