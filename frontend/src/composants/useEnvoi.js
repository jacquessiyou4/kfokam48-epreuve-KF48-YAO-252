import { useState } from 'react'

// Envoi d'un formulaire : état « en cours », message de succès ou erreur de l'API (F3).
export function useEnvoi() {
  const [etat, setEtat] = useState({ envoi: false, succes: null, erreur: null })

  async function envoyer(appel, messageSucces) {
    setEtat({ envoi: true, succes: null, erreur: null })
    try {
      const resultat = await appel()
      setEtat({ envoi: false, succes: messageSucces(resultat), erreur: null })
      return resultat
    } catch (erreur) {
      setEtat({ envoi: false, succes: null, erreur })
      return null
    }
  }

  return { ...etat, envoyer }
}
