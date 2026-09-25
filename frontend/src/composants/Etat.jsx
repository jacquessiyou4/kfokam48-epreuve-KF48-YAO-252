// Affichage homogène du chargement et des erreurs de l'API (F3).
export function Chargement() {
  return <p aria-live="polite">Chargement…</p>
}

// onReessayer : pour une erreur de chargement, relance l'appel sans recharger la page.
export function MessageErreur({ erreur, onReessayer }) {
  if (!erreur) return null
  return (
    <p className="erreur" role="alert">
      {erreur.message}
      {onReessayer && <>{' '}<button type="button" className="lien" onClick={onReessayer}>Réessayer</button></>}
    </p>
  )
}
