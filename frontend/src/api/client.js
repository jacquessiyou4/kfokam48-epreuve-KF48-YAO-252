// Couche d'accès à l'API (F3) : tous les appels passent par ici, aucun fetch ailleurs.
// Une erreur de l'API ({ code, message }) est levée sous forme d'ErreurApi.

export class ErreurApi extends Error {
  constructor(statut, code, message) {
    super(message)
    this.statut = statut
    this.code = code
  }
}

async function requete(chemin, { methode = 'GET', corps } = {}) {
  let reponse
  try {
    reponse = await fetch(`/api${chemin}`, {
      method: methode,
      headers: corps ? { 'Content-Type': 'application/json' } : undefined,
      body: corps ? JSON.stringify(corps) : undefined,
    })
  } catch {
    throw new ErreurApi(0, 'RESEAU', 'Serveur injoignable. Vérifiez votre connexion.')
  }
  const texte = await reponse.text()
  const donnees = texte ? JSON.parse(texte) : null
  if (!reponse.ok) {
    throw new ErreurApi(reponse.status, donnees?.code ?? 'INCONNUE', donnees?.message ?? `Erreur ${reponse.status}`)
  }
  return donnees
}

// EF2 — promotions et étudiants
export const listerPromotions = () => requete('/promotions')
export const listerEtudiants = (promotionId) => requete(`/promotions/${promotionId}/etudiants`)

// EF1 — sessions
export const ouvrirSession = (titre, promotionId) =>
  requete('/sessions', { methode: 'POST', corps: { titre, promotionId: Number(promotionId) } })
export const listerSessions = (promotionId) => requete(`/sessions?promotionId=${promotionId}`)

// EF9 — tableau du formateur
export const chargerTableau = (promotionId) => requete(`/tableau?promotionId=${promotionId}`)

// EF3 — présence par code
export const marquerPresence = (code, etudiantId) =>
  requete('/presences', { methode: 'POST', corps: { code, etudiantId } })

// EF5 — dépôt d'exercice
export const deposerExercice = (sessionId, etudiantId, lien) =>
  requete('/exercices', { methode: 'POST', corps: { sessionId: Number(sessionId), etudiantId, lien } })

// EF7, EF8 — relectures
export const listerRelectures = (etudiantId) => requete(`/etudiants/${etudiantId}/relectures`)
export const rendreRelecture = (relectureId, note, commentaire, relecteurId) =>
  requete(`/relectures/${relectureId}`, { methode: 'POST', corps: { note: Number(note), commentaire, relecteurId } })
