// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { appels, connecter, simulerApi } from '../test/apiSimulee.js'
import EcranRelecteur from './EcranRelecteur.jsx'

// Intégration de l'écran relecteur (F2, F3) : la note est jugée par l'API, pas par l'écran.
const A_RELIRE = { id: 9, sessionTitre: 'Spring Boot — jour 2', lien: 'https://github.com/nana/tp2', rendue: false }

beforeEach(() => {
  localStorage.clear()
  connecter()
})
afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

async function rendreUneRelecture(utilisateur, note, commentaire) {
  await utilisateur.type(await screen.findByLabelText('Note sur 20'), note)
  await utilisateur.type(screen.getByLabelText('Commentaire'), commentaire)
  await utilisateur.click(screen.getByRole('button', { name: 'Envoyer (définitif)' }))
}

describe('écran relecteur', () => {
  it("rend la relecture avec l'identifiant du relecteur, puis affiche la relecture rendue (EF8)", async () => {
    let rendue = false
    const fetch = simulerApi({
      'GET /api/promotions': [200, []],
      'GET /api/etudiants/4/relectures': () => [200, [rendue
        ? { ...A_RELIRE, rendue: true, note: 15, commentaire: 'Bon découpage.' }
        : A_RELIRE]],
      'POST /api/relectures/9': () => {
        rendue = true
        return [200, { id: 9, note: 15 }]
      },
    })
    const utilisateur = userEvent.setup()
    render(<EcranRelecteur />)

    expect((await screen.findByRole('link', { name: "ouvrir l'exercice" })).getAttribute('rel')).toBe('noreferrer')
    await rendreUneRelecture(utilisateur, '15', 'Bon découpage.')

    expect(await screen.findByText('Rendue : 15/20 — « Bon découpage. »')).toBeTruthy()
    const [[, options]] = appels(fetch, 'POST /api/relectures/9')
    expect(JSON.parse(options.body)).toEqual({ note: 15, commentaire: 'Bon découpage.', relecteurId: 4 })
    expect(screen.queryByLabelText('Note sur 20')).toBeNull()
  })

  it.each([
    [400, 'NOTE_INVALIDE', 'La note doit être un entier entre 0 et 20.', 'RG10'],
    [409, 'RELECTURE_DEJA_RENDUE', 'Cette relecture a déjà été rendue.', 'RG11'],
  ])("affiche le refus %i %s de l'API et garde le formulaire (%s)", async (statut, code, message) => {
    simulerApi({
      'GET /api/promotions': [200, []],
      'GET /api/etudiants/4/relectures': [200, [A_RELIRE]],
      'POST /api/relectures/9': [statut, { code, message }],
    })
    const utilisateur = userEvent.setup()
    render(<EcranRelecteur />)

    await rendreUneRelecture(utilisateur, '12', 'Correct.')

    expect((await screen.findByRole('alert')).textContent).toBe(message)
    expect(screen.getByLabelText('Note sur 20').value).toBe('12')
  })

  it("signale qu'aucune relecture n'est assignée", async () => {
    simulerApi({ 'GET /api/promotions': [200, []], 'GET /api/etudiants/4/relectures': [200, []] })
    render(<EcranRelecteur />)

    expect(await screen.findByText('Aucune relecture ne vous est assignée.')).toBeTruthy()
  })
})

describe('écran relecteur — serveur injoignable', () => {
  it('explique que le serveur est injoignable, puis recharge la liste avec « Réessayer » (F3)', async () => {
    let backendDemarre = false
    simulerApi({
      'GET /api/promotions': [200, []],
      'GET /api/etudiants/4/relectures': () => (backendDemarre ? [200, [A_RELIRE]] : [502, undefined]),
    })
    const utilisateur = userEvent.setup()
    render(<EcranRelecteur />)

    const alerte = await screen.findByRole('alert')
    expect(alerte.textContent).toContain('Serveur injoignable')

    backendDemarre = true
    await utilisateur.click(screen.getByRole('button', { name: 'Réessayer' }))

    expect(await screen.findByRole('link', { name: "ouvrir l'exercice" })).toBeTruthy()
    expect(screen.queryByRole('alert')).toBeNull()
  })
})
