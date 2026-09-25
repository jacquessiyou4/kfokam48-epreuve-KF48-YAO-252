import { useState } from 'react'
import { listerRelectures, rendreRelecture } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import ChoixEtudiant from '../composants/ChoixEtudiant.jsx'
import { Chargement, MessageErreur } from '../composants/Etat.jsx'
import { useEnvoi } from '../composants/useEnvoi.js'

// EF8 — les règles (entier 0–20, relecture définitive…) sont vérifiées par l'API, pas ici (F3)
function FormulaireRelecture({ relecture, relecteurId, onRendue }) {
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const envoi = useEnvoi()

  async function soumettre(e) {
    e.preventDefault()
    const ok = await envoi.envoyer(() => rendreRelecture(relecture.id, note, commentaire, relecteurId),
      () => 'Relecture envoyée.')
    if (ok) onRendue()
  }

  return (
    <form onSubmit={soumettre}>
      <label htmlFor={`note-${relecture.id}`}>Note sur 20</label>
      <input id={`note-${relecture.id}`} type="number" inputMode="numeric" min="0" max="20" step="1"
        value={note} onChange={(e) => setNote(e.target.value)} required />
      <label htmlFor={`commentaire-${relecture.id}`}>Commentaire</label>
      <textarea id={`commentaire-${relecture.id}`} rows={3} maxLength={2000} value={commentaire}
        onChange={(e) => setCommentaire(e.target.value)} required />
      <button type="submit" disabled={envoi.envoi}>{envoi.envoi ? 'Envoi…' : 'Envoyer (définitif)'}</button>
      <MessageErreur erreur={envoi.erreur} />
    </form>
  )
}

function MesRelectures({ etudiant }) {
  const relectures = useAppel(() => listerRelectures(etudiant.id), [etudiant.id])

  if (relectures.chargement) return <Chargement />
  if (relectures.erreur) return <MessageErreur erreur={relectures.erreur} onReessayer={relectures.recharger} />
  if (relectures.donnees.length === 0) return <p>Aucune relecture ne vous est assignée.</p>

  return (
    <ul className="relectures">
      {relectures.donnees.map((r) => (
        <li key={r.id}>
          <p><strong>{r.sessionTitre}</strong> — <a href={r.lien} target="_blank" rel="noreferrer">ouvrir l'exercice</a></p>
          {r.rendue
            ? <p className="succes">Rendue : {r.note}/20 — « {r.commentaire} »</p>
            : <FormulaireRelecture relecture={r} relecteurId={etudiant.id} onRendue={relectures.recharger} />}
        </li>
      ))}
    </ul>
  )
}

export default function EcranRelecteur() {
  return (
    <section>
      <h2>Espace relecteur</h2>
      <ChoixEtudiant>{(etudiant) => <MesRelectures etudiant={etudiant} />}</ChoixEtudiant>
    </section>
  )
}
