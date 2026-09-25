import { listerPromotions } from '../api/client.js'
import { useAppel } from '../api/useAppel.js'
import { Chargement, MessageErreur } from './Etat.jsx'

export default function ChoixPromotion({ valeur, onChange }) {
  const promotions = useAppel(listerPromotions, [])
  if (promotions.chargement) return <Chargement />
  if (promotions.erreur) return <MessageErreur erreur={promotions.erreur} onReessayer={promotions.recharger} />
  return (
    <>
      <label htmlFor="promotion">Promotion</label>
      <select id="promotion" value={valeur} onChange={(e) => onChange(e.target.value)}>
        <option value="">— choisir —</option>
        {promotions.donnees.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
      </select>
    </>
  )
}
