import { useState } from 'react'
import { listerEtudiants, listerPromotions } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import { Chargement, MessageErreur } from './Etat.jsx'

const CLE = 'etudiantChoisi'

function lireChoix() {
  try {
    return JSON.parse(localStorage.getItem(CLE))
  } catch {
    return null
  }
}

function memoriser(etudiant) {
  try {
    if (etudiant) localStorage.setItem(CLE, JSON.stringify(etudiant))
    else localStorage.removeItem(CLE)
  } catch {
    // stockage indisponible (navigation privée) : le choix vaut pour la page en cours
  }
}

// EF2 — l'étudiant se choisit dans la liste de sa promotion, sans mot de passe (Q1).
// Le choix est mémorisé pour ne pas avoir à le refaire à chaque action.
export default function ChoixEtudiant({ children }) {
  const [etudiant, setEtudiant] = useState(lireChoix)
  const [promotionId, setPromotionId] = useState('')
  const promotions = useAppel(listerPromotions, [])
  const etudiants = useAppel(
    () => (promotionId ? listerEtudiants(promotionId) : Promise.resolve([])),
    [promotionId],
  )

  function choisir(e) {
    const choisi = etudiants.donnees.find((x) => String(x.id) === e.target.value)
    memoriser(choisi)
    setEtudiant(choisi)
  }

  function changer() {
    memoriser(null)
    setEtudiant(null)
  }

  if (etudiant) {
    return (
      <>
        <p>
          Connecté en tant que <strong>{etudiant.nom}</strong>{' '}
          <button type="button" className="lien" onClick={changer}>Changer</button>
        </p>
        {children(etudiant)}
      </>
    )
  }

  return (
    <section>
      <h2>Qui êtes-vous ?</h2>
      {promotions.chargement ? <Chargement /> : <MessageErreur erreur={promotions.erreur} />}
      {promotions.donnees && (
        <>
          <label htmlFor="promotion">Promotion</label>
          <select id="promotion" value={promotionId} onChange={(e) => setPromotionId(e.target.value)}>
            <option value="">— choisir —</option>
            {promotions.donnees.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
          </select>
        </>
      )}
      {promotionId && (etudiants.chargement ? <Chargement /> : <MessageErreur erreur={etudiants.erreur} />)}
      {promotionId && etudiants.donnees && (
        <>
          <label htmlFor="etudiant">Votre nom</label>
          <select id="etudiant" defaultValue="" onChange={choisir}>
            <option value="" disabled>— choisir —</option>
            {etudiants.donnees.map((x) => <option key={x.id} value={x.id}>{x.nom}</option>)}
          </select>
        </>
      )}
    </section>
  )
}
