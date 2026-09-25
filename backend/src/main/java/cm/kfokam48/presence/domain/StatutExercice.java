package cm.kfokam48.presence.domain;

/** Cycle de vie d'un exercice, voir D4. */
public enum StatutExercice {
    EN_ATTENTE_RELECTEUR,
    EN_ATTENTE_RELECTURE,
    /** Une relecture rendue sur deux : note retenue provisoire (RG23, D4 v2). */
    RELU_PARTIELLEMENT,
    RELU
}
