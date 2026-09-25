package cm.kfokam48.presence.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercice")
public class Exercice {

    /** RG7 v2 — nombre de relecteurs d'un exercice déposé depuis le changement de besoin. */
    public static final int RELECTEURS_REQUIS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    /** L'auteur de l'exercice. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;

    @Column(name = "modifie_at")
    private Instant modifieAt;

    /** 2 pour un nouveau dépôt ; 1 pour les exercices déposés avant la migration V3 (H13). */
    @Column(name = "relecteurs_requis", nullable = false)
    private int relecteursRequis;

    protected Exercice() {
    }

    public Exercice(SessionCours session, Etudiant etudiant, String lien, Instant deposeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.deposeAt = deposeAt;
        this.statut = StatutExercice.EN_ATTENTE_RELECTEUR;
        this.relecteursRequis = RELECTEURS_REQUIS;
    }

    /** T3 / T5 de D4 : au moins un relecteur est affecté ; ne fait pas reculer un exercice déjà relu. */
    public void marquerRelecteurAffecte() {
        if (statut == StatutExercice.EN_ATTENTE_RELECTEUR) {
            this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
        }
    }

    /** T7a / T7b de D4 v2 : relu partiellement tant qu'il manque une relecture requise, puis relu. */
    public void enregistrerRelecturesRendues(int rendues) {
        this.statut = rendues >= relecteursRequis ? StatutExercice.RELU : StatutExercice.RELU_PARTIELLEMENT;
    }

    /** T6 de D4 : le statut ne change pas. */
    public void remplacerLien(String nouveauLien, Instant instant) {
        this.lien = nouveauLien;
        this.modifieAt = instant;
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }

    public Instant getModifieAt() {
        return modifieAt;
    }

    public int getRelecteursRequis() {
        return relecteursRequis;
    }
}
