import { useState } from 'react'
import {
  ajouterPresence, chargerTableau, cloturerSession, listerEtudiants, listerSessions, ouvrirSession,
} from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import ChoixPromotion from '../composants/ChoixPromotion.jsx'
import { Chargement, MessageErreur } from '../composants/Etat.jsx'
import { useEnvoi } from '../composants/useEnvoi.js'

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

// EF10 — une session clôturée n'accepte plus ni présence, ni dépôt, ni relecture
// EF4 — le formateur ajoute une présence (Q14), marquée source = FORMATEUR
function AjouterPresence({ session, etudiants, onChange }) {
  const [etudiantId, setEtudiantId] = useState('')
  const envoi = useEnvoi()

  async function soumettre(e) {
    e.preventDefault()
    if (await envoi.envoyer(() => ajouterPresence(session.id, etudiantId), () => 'Présence ajoutée par le formateur.')) {
      setEtudiantId('')
      onChange()
    }
  }

  return (
    <form onSubmit={soumettre}>
      <label htmlFor={`ajout-${session.id}`}>Ajouter une présence à la main</label>
      <select id={`ajout-${session.id}`} value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)} required>
        <option value="">— étudiant —</option>
        {etudiants.map((x) => <option key={x.id} value={x.id}>{x.nom}</option>)}
      </select>
      <button type="submit" disabled={envoi.envoi}>Ajouter</button>
      {envoi.succes && <p className="succes" role="status">{envoi.succes}</p>}
      <MessageErreur erreur={envoi.erreur} />
    </form>
  )
}

function Session({ session, etudiants, onChange }) {
  const envoi = useEnvoi()

  async function cloturer() {
    if (await envoi.envoyer(() => cloturerSession(session.id), () => 'Session clôturée.')) onChange()
  }

  return (
    <li>
      <p>
        <strong>{session.titre}</strong> — code {session.code}{' '}
        {session.cloturee ? <em>(clôturée)</em> : (
          <button type="button" className="lien" onClick={cloturer} disabled={envoi.envoi}>Clôturer</button>
        )}
      </p>
      <MessageErreur erreur={envoi.erreur} />
      {!session.cloturee && <AjouterPresence session={session} etudiants={etudiants} onChange={onChange} />}
    </li>
  )
}

function Sessions({ sessions, etudiants, onChange }) {
  if (sessions.chargement) return <Chargement />
  if (sessions.erreur) return <MessageErreur erreur={sessions.erreur} />
  if (sessions.donnees.length === 0) return <p>Aucune session.</p>
  return (
    <ul className="relectures">
      {sessions.donnees.map((s) => <Session key={s.id} session={s} etudiants={etudiants} onChange={onChange} />)}
    </ul>
  )
}

// EF9 — la moyenne est affichée telle que l'API la calcule (F3)
export function Tableau({ tableau }) {
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
              <td>
                {l.presences}
                {l.presencesAjouteesParFormateur > 0 && (
                  <span className="provisoire"> dont {l.presencesAjouteesParFormateur} ajoutée(s) par le formateur</span>
                )}
              </td>
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
  const sessions = useAppel(
    () => (promotionId ? listerSessions(promotionId) : Promise.resolve([])),
    [promotionId],
  )
  const etudiants = useAppel(
    () => (promotionId ? listerEtudiants(promotionId) : Promise.resolve([])),
    [promotionId],
  )

  function actualiser() {
    tableau.recharger()
    sessions.recharger()
  }

  return (
    <section>
      <h2>Espace formateur</h2>
      <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
      {promotionId && (
        <>
          <OuvrirSession promotionId={promotionId} onOuverte={actualiser} />
          <h3>Sessions</h3>
          <Sessions sessions={sessions} etudiants={etudiants.donnees ?? []} onChange={actualiser} />
          <h3>Tableau de la promotion <button type="button" className="lien" onClick={actualiser}>Actualiser</button></h3>
          <Tableau tableau={tableau} />
        </>
      )}
    </section>
  )
}
