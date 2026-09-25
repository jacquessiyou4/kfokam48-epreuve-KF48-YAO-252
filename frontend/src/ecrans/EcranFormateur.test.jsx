// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { appels, simulerApi } from '../test/apiSimulee.js'
import EcranFormateur from './EcranFormateur.jsx'

// Intégration de l'écran formateur : la clôture, définitive, se confirme avant l'appel (EF10).
afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

function simulerPromotion(cloture) {
  return simulerApi({
    'GET /api/promotions': [200, [{ id: 1, nom: 'KF48 Yaoundé' }]],
    'GET /api/promotions/1/etudiants': [200, []],
    'GET /api/tableau?promotionId=1': [200, []],
    'GET /api/sessions?promotionId=1': () => [200, [{ id: 3, titre: 'Spring Boot — jour 2', code: 'K7P2QX', cloturee: cloture.faite }]],
    'POST /api/sessions/3/cloture': () => {
      cloture.faite = true
      return [200, { id: 3, cloturee: true }]
    },
  })
}

async function ouvrirPromotion(utilisateur) {
  render(<EcranFormateur />)
  await utilisateur.selectOptions(await screen.findByLabelText('Promotion'), '1')
  return screen.findByRole('button', { name: 'Clôturer' })
}

describe('écran formateur — clôture', () => {
  it("n'appelle pas l'API tant que la clôture n'est pas confirmée, et « Annuler » ne fait rien", async () => {
    const fetch = simulerPromotion({ faite: false })
    const utilisateur = userEvent.setup()

    await utilisateur.click(await ouvrirPromotion(utilisateur))
    expect(screen.getByRole('group', { name: 'Confirmer la clôture' }).textContent).toContain('Spring Boot — jour 2')
    await utilisateur.click(screen.getByRole('button', { name: 'Annuler' }))

    expect(appels(fetch, 'POST /api/sessions/3/cloture')).toHaveLength(0)
    expect(screen.getByRole('button', { name: 'Clôturer' })).toBeTruthy()
  })

  it('clôture après confirmation et affiche la session clôturée (EF10, RG2)', async () => {
    const fetch = simulerPromotion({ faite: false })
    const utilisateur = userEvent.setup()

    await utilisateur.click(await ouvrirPromotion(utilisateur))
    await utilisateur.click(screen.getByRole('button', { name: 'Oui, clôturer' }))

    expect(await screen.findByText('(clôturée)')).toBeTruthy()
    expect(appels(fetch, 'POST /api/sessions/3/cloture')).toHaveLength(1)
  })
})
