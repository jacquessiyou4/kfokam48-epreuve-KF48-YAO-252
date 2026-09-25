package cm.kfokam48.presence.domain;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_cours")
public class SessionCours {

    /** RG1 — durée de validité du code de présence. */
    public static final Duration VALIDITE_CODE = Duration.ofMinutes(15);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false, length = 6, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(name = "cloturee_at")
    private Instant clotureeAt;

    protected SessionCours() {
    }

    public SessionCours(String titre, Promotion promotion, String code, Instant ouvertureAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = ouvertureAt.plus(VALIDITE_CODE);
    }

    /** RG1 : le code ne marche plus après expiration ; RG2 : ni après clôture. */
    public boolean codeUtilisableA(Instant instant) {
        return !estCloturee() && instant.isBefore(expirationAt);
    }

    public boolean estCloturee() {
        return clotureeAt != null;
    }

    public void cloturer(Instant instant) {
        this.clotureeAt = instant;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public Instant getClotureeAt() {
        return clotureeAt;
    }
}
