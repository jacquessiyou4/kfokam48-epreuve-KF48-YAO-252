import { useState } from 'react'
import { deposerExercice, listerMesExercices, listerSessions, marquerPresence } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import ChoixEtudiant from '../composants/ChoixEtudiant.jsx'
import { Chargement, MessageErreur } from '../composants/Etat.jsx'
import { useEnvoi } from '../composants/useEnvoi.js'

const LIBELLES_STATUT = {
  EN_ATTENTE_RELECTURE: 'des camarades ont été désignés pour le relire',
  EN_ATTENTE_RELECTEUR: 'aucun camarade présent pour le relire pour l’instant, ils seront désignés dès que des étudiants arrivent',
  RELU_PARTIELLEMENT: 'une relecture sur deux rendue',
  RELU: 'relu',
}

// EF11 v2 — la note retenue et son caractère provisoire viennent de l'API (F3)
function MesExercices({ etudiant, version }) {
  const exercices = useAppel(() => listerMesExercices(etudiant.id), [etudiant.id, version])
  if (exercices.chargement) return <Chargement />
  if (exercices.erreur) return <MessageErreur erreur={exercices.erreur} />
  return (
    <section>
      <h3>Mes exercices</h3>
      {exercices.donnees.length === 0 ? <p>Aucun exercice déposé.</p> : (
        <ul className="relectures">
          {exercices.donnees.map((e) => (
            <li key={e.id}>
              <p><strong>{e.sessionTitre}</strong> — {LIBELLES_STATUT[e.statut] ?? e.statut}</p>
              {e.noteRetenue !== null && (
                <p>
                  Note retenue : <strong>{e.noteRetenue}/20</strong>{' '}
                  {e.noteProvisoire && <span className="provisoire">provisoire — en attente de la seconde relecture</span>}
                </p>
              )}
              {e.commentaires.map((c, i) => <p key={i} className="commentaire">« {c} »</p>)}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
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
function DeposerExercice({ etudiant, onDepose }) {
  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const sessions = useAppel(() => listerSessions(etudiant.promotionId), [etudiant.promotionId])
  const envoi = useEnvoi()

  async function soumettre(e) {
    e.preventDefault()
    const ok = await envoi.envoyer(() => deposerExercice(sessionId, etudiant.id, lien),
      (r) => `Exercice déposé : ${LIBELLES_STATUT[r.statut] ?? r.statut}.`)
    if (ok) {
      setLien('')
      onDepose()
    }
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
  const [version, setVersion] = useState(0)
  return (
    <section>
      <h2>Espace étudiant</h2>
      <ChoixEtudiant>
        {(etudiant) => (
          <>
            <MarquerPresence etudiant={etudiant} />
            <DeposerExercice etudiant={etudiant} onDepose={() => setVersion((v) => v + 1)} />
            <MesExercices etudiant={etudiant} version={version} />
          </>
        )}
      </ChoixEtudiant>
    </section>
  )
}
