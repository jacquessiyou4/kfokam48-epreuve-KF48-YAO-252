// Affichage homogène du chargement et des erreurs de l'API (F3).
export function Chargement() {
  return <p aria-live="polite">Chargement…</p>
}

export function MessageErreur({ erreur }) {
  if (!erreur) return null
  return <p className="erreur" role="alert">{erreur.message}</p>
}
