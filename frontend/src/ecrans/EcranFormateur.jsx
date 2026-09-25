import { useState } from 'react'
import { chargerTableau, ouvrirSession } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import ChoixPromotion from '../composants/ChoixPromotion.jsx'
import { Chargement, MessageErreur } from '../composants/Etat.jsx'

const heure = (iso) => new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })

// EF1 — ouvrir une session et afficher son code
function OuvrirSession({ promotionId, onOuverte }) {
  const [titre, setTitre] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [session, setSession] = useState(null)

  async function soumettre(e) {
    e.preventDefault()
    setEnvoi(true)
    setErreur(null)
    try {
      setSession(await ouvrirSession(titre, promotionId))
      setTitre('')
      onOuverte()
    } catch (err) {
      setErreur(err)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <section>
      <h3>Ouvrir une session</h3>
      <form onSubmit={soumettre}>
        <label htmlFor="titre">Titre du cours</label>
        <input id="titre" value={titre} onChange={(e) => setTitre(e.target.value)} required maxLength={200} />
        <button type="submit" disabled={envoi}>{envoi ? 'Ouverture…' : 'Ouvrir la session'}</button>
      </form>
      <MessageErreur erreur={erreur} />
      {session && (
        <div className="code-presence">
          <p>Code de présence à donner aux étudiants :</p>
          <p className="code">{session.code}</p>
          <p>Valable jusqu'à {heure(session.expirationAt)}</p>
        </div>
      )}
    </section>
  )
}

// EF9 — la moyenne est affichée telle que l'API la calcule (F3)
function Tableau({ tableau }) {
  if (tableau.chargement) return <Chargement />
  if (tableau.erreur) return <MessageErreur erreur={tableau.erreur} />
  if (tableau.donnees.length === 0) return <p>Aucun étudiant dans cette promotion.</p>
  return (
    <div className="tableau-conteneur">
      <table>
        <thead>
          <tr>
            <th>Étudiant</th><th>Présences</th><th>Exercices déposés</th>
            <th>Moyenne reçue</th><th>Exercices en attente de relecture</th><th>Relectures à faire</th>
          </tr>
        </thead>
        <tbody>
          {tableau.donnees.map((l) => (
            <tr key={l.etudiantId}>
              <td>{l.nom}</td>
              <td>{l.presences}</td>
              <td>{l.exercicesDeposes}</td>
              <td>
                {l.moyenne ?? '—'}
                {l.moyenneProvisoire && <span className="provisoire"> provisoire</span>}
              </td>
              <td className={l.exercicesEnAttente > 0 ? 'attente' : undefined}>{l.exercicesEnAttente}</td>
              <td className={l.relecturesEnAttente > 0 ? 'attente' : undefined}>{l.relecturesEnAttente}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function EcranFormateur() {
  const [promotionId, setPromotionId] = useState('')
  const tableau = useAppel(
    () => (promotionId ? chargerTableau(promotionId) : Promise.resolve([])),
    [promotionId],
  )

  return (
    <section>
      <h2>Espace formateur</h2>
      <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
      {promotionId && (
        <>
          <OuvrirSession promotionId={promotionId} onOuverte={tableau.recharger} />
          <h3>Tableau de la promotion <button type="button" className="lien" onClick={tableau.recharger}>Actualiser</button></h3>
          <Tableau tableau={tableau} />
        </>
      )}
    </section>
  )
}
