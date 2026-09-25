import { useCallback, useEffect, useState } from 'react'

// Charge une donnée de l'API et expose les états chargement / erreur (F3).
export function useAppel(appel, dependances) {
  const [etat, setEtat] = useState({ donnees: null, chargement: true, erreur: null })

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const executer = useCallback(appel, dependances)

  const recharger = useCallback(() => {
    setEtat((e) => ({ ...e, chargement: true, erreur: null }))
    return executer()
      .then((donnees) => setEtat({ donnees, chargement: false, erreur: null }))
      .catch((erreur) => setEtat({ donnees: null, chargement: false, erreur }))
  }, [executer])

  useEffect(() => {
    recharger()
  }, [recharger])

  return { ...etat, recharger }
}
