import { useState } from 'react'
import { deposerExercice, listerSessions, marquerPresence } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import ChoixEtudiant from '../composants/ChoixEtudiant.jsx'
import { Chargement, MessageErreur } from '../composants/Etat.jsx'
import { useEnvoi } from '../composants/useEnvoi.js'

const LIBELLES_STATUT = {
  EN_ATTENTE_RELECTURE: 'un camarade a été désigné pour le relire',
  EN_ATTENTE_RELECTEUR: 'aucun camarade présent pour le relire pour l’instant, il sera désigné dès qu’un étudiant arrive',
}

// EF3 — marquer sa présence avec le code
function MarquerPresence({ etudiant }) {
  const [code, setCode] = useState('')
  const envoi = useEnvoi()

  async function soumettre(e) {
    e.preventDefault()
    const ok = await envoi.envoyer(() => marquerPresence(code, etudiant.id), () => 'Présence enregistrée.')
    if (ok) setCode('')
  }

  return (
    <section>
      <h3>Marquer ma présence</h3>
      <form onSubmit={soumettre}>
        <label htmlFor="code">Code donné par le formateur</label>
        <input id="code" value={code} onChange={(e) => setCode(e.target.value)} required
          autoComplete="off" autoCapitalize="characters" maxLength={6} className="saisie-code" />
        <button type="submit" disabled={envoi.envoi}>{envoi.envoi ? 'Envoi…' : 'Je suis présent'}</button>
      </form>
      {envoi.succes && <p className="succes" role="status">{envoi.succes}</p>}
      <MessageErreur erreur={envoi.erreur} />
    </section>
  )
}

// EF5 — déposer le lien de son exercice pour une session non clôturée
function DeposerExercice({ etudiant }) {
  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const sessions = useAppel(() => listerSessions(etudiant.promotionId), [etudiant.promotionId])
  const envoi = useEnvoi()

  async function soumettre(e) {
    e.preventDefault()
    const ok = await envoi.envoyer(() => deposerExercice(sessionId, etudiant.id, lien),
      (r) => `Exercice déposé : ${LIBELLES_STATUT[r.statut] ?? r.statut}.`)
    if (ok) setLien('')
  }

  if (sessions.chargement) return <Chargement />
  if (sessions.erreur) return <MessageErreur erreur={sessions.erreur} />
  const ouvertes = sessions.donnees.filter((s) => !s.cloturee)

  return (
    <section>
      <h3>Déposer mon exercice</h3>
      {ouvertes.length === 0 ? <p>Aucune session ouverte au dépôt.</p> : (
        <form onSubmit={soumettre}>
          <label htmlFor="session">Session</label>
          <select id="session" value={sessionId} onChange={(e) => setSessionId(e.target.value)} required>
            <option value="">— choisir —</option>
            {ouvertes.map((s) => <option key={s.id} value={s.id}>{s.titre}</option>)}
          </select>
          <label htmlFor="lien">Lien de l'exercice</label>
          <input id="lien" type="url" inputMode="url" placeholder="https://github.com/…" value={lien}
            onChange={(e) => setLien(e.target.value)} required />
          <button type="submit" disabled={envoi.envoi}>{envoi.envoi ? 'Envoi…' : 'Déposer'}</button>
        </form>
      )}
      {envoi.succes && <p className="succes" role="status">{envoi.succes}</p>}
      <MessageErreur erreur={envoi.erreur} />
    </section>
  )
}

export default function EcranEtudiant() {
  return (
    <section>
      <h2>Espace étudiant</h2>
      <ChoixEtudiant>
        {(etudiant) => (
          <>
            <MarquerPresence etudiant={etudiant} />
            <DeposerExercice etudiant={etudiant} />
          </>
        )}
      </ChoixEtudiant>
    </section>
  )
}
