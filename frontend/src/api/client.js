// Couche d'accès à l'API (F3) : tous les appels passent par ici, aucun fetch ailleurs.
// Une erreur de l'API ({ code, message }) est levée sous forme d'ErreurApi.

export class ErreurApi extends Error {
  constructor(statut, code, message) {
    super(message)
    this.statut = statut
    this.code = code
  }
}

// Réponse sans JSON en 5xx : c'est le relais (nginx, Vite) qui répond à la place d'un backend arrêté ;
// le backend, lui, renvoie toujours { code, message }.
function erreurSansJson(statut) {
  return statut >= 500
    ? new ErreurApi(statut, 'SERVEUR_INJOIGNABLE', 'Serveur injoignable. Réessayez dans un instant.')
    : new ErreurApi(statut, 'REPONSE_INVALIDE', `Réponse inattendue du serveur (statut ${statut}).`)
}

function lireJson(texte) {
  if (!texte) return null
  try {
    return JSON.parse(texte)
  } catch {
    return undefined
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
  const donnees = lireJson(await reponse.text())
  if (donnees === undefined || (donnees === null && reponse.status >= 500)) throw erreurSansJson(reponse.status)
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

// EF11 v2 — mes exercices, note retenue provisoire ou définitive
export const listerMesExercices = (etudiantId) => requete(`/etudiants/${etudiantId}/exercices`)

// EF10 — clôture de session
export const cloturerSession = (sessionId) => requete(`/sessions/${sessionId}/cloture`, { methode: 'POST' })

// EF4 — présence ajoutée par le formateur (Q14)
export const ajouterPresence = (sessionId, etudiantId) =>
  requete(`/sessions/${sessionId}/presences`, { methode: 'POST', corps: { etudiantId: Number(etudiantId) } })
