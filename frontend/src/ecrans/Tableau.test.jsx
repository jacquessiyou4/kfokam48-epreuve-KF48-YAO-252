import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import { Tableau } from './EcranFormateur.jsx'

// EF9, RG17 — la moyenne vient de l'API, l'écran ne la recalcule pas (F3).
const ligne = (champs) => ({
  etudiantId: 1, nom: 'FOTSO Marie', presences: 2, presencesAjouteesParFormateur: 0,
  exercicesDeposes: 2, moyenne: null, moyenneProvisoire: false, exercicesEnAttente: 0, relecturesEnAttente: 0,
  ...champs,
})
const afficher = (tableau) => renderToStaticMarkup(<Tableau tableau={tableau} />)

describe('tableau du formateur', () => {
  it("affiche la moyenne telle que l'API la renvoie, sans la recalculer", () => {
    // 17.25 ne peut venir d'aucun calcul fait à l'écran : aucune note n'est transmise.
    const html = afficher({ chargement: false, erreur: null, donnees: [ligne({ moyenne: 17.25 })] })

    expect(html).toContain('17.25')
    expect(html).not.toContain('provisoire')
  })

  it('marque la moyenne provisoire quand l’API le dit (RG23)', () => {
    const html = afficher({ chargement: false, erreur: null, donnees: [ligne({ moyenne: 13.5, moyenneProvisoire: true })] })

    expect(html).toMatch(/13\.5<span class="provisoire"> provisoire<\/span>/)
  })

  it('affiche un tiret quand aucune note n’est rendue', () => {
    const html = afficher({ chargement: false, erreur: null, donnees: [ligne({ moyenne: null })] })

    expect(html).toContain('<td>—</td>')
  })

  it('affiche le chargement puis le message d’erreur de l’API', () => {
    expect(afficher({ chargement: true, erreur: null, donnees: null })).toContain('Chargement…')

    const html = afficher({ chargement: false, erreur: { message: 'Promotion inconnue.' }, donnees: null })
    expect(html).toContain('role="alert"')
    expect(html).toContain('Promotion inconnue.')
  })

  it('signale une promotion sans étudiant', () => {
    expect(afficher({ chargement: false, erreur: null, donnees: [] })).toContain('Aucun étudiant dans cette promotion.')
  })
})
