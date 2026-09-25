// @vitest-environment jsdom
import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { appels, connecter, ETUDIANT, simulerApi } from '../test/apiSimulee.js'
import EcranEtudiant from './EcranEtudiant.jsx'

// Intégration de l'écran étudiant (F2, F3) : saisie → vraie couche API → affichage.
const SESSIONS = [
  { id: 3, titre: 'Spring Boot — jour 2', cloturee: false },
  { id: 2, titre: 'Spring Boot — jour 1', cloturee: true },
]
const routesDeBase = (autres = {}) => ({
  'GET /api/promotions': [200, [{ id: 1, nom: 'KF48 Yaoundé' }]],
  'GET /api/promotions/1/etudiants': [200, [ETUDIANT, { id: 5, nom: 'NANA Paul', promotionId: 1 }]],
  'GET /api/sessions?promotionId=1': [200, SESSIONS],
  'GET /api/etudiants/4/exercices': [200, []],
  ...autres,
})

beforeEach(() => localStorage.clear())
afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe('écran étudiant', () => {
  it("l'étudiant se choisit dans sa promotion, sans mot de passe, et son choix est mémorisé (EF2)", async () => {
    simulerApi(routesDeBase())
    const utilisateur = userEvent.setup()
    render(<EcranEtudiant />)

    await utilisateur.selectOptions(await screen.findByLabelText('Promotion'), '1')
    await utilisateur.selectOptions(await screen.findByLabelText('Votre nom'), '4')

    expect(await screen.findByText('FOTSO Marie')).toBeTruthy()
    expect(screen.getByText(/Connecté en tant que/)).toBeTruthy()
    expect(JSON.parse(localStorage.getItem('etudiantChoisi'))).toEqual(ETUDIANT)
  })

  it('marque la présence avec le code et vide le champ (EF3)', async () => {
    connecter()
    const fetch = simulerApi(routesDeBase({ 'POST /api/presences': [201, { id: 11, source: 'ETUDIANT' }] }))
    const utilisateur = userEvent.setup()
    render(<EcranEtudiant />)

    const champ = await screen.findByLabelText('Code donné par le formateur')
    await utilisateur.type(champ, 'K7P2QX')
    await utilisateur.click(screen.getByRole('button', { name: 'Je suis présent' }))

    expect((await screen.findByRole('status')).textContent).toBe('Présence enregistrée.')
    expect(champ.value).toBe('')
    const [[, options]] = appels(fetch, 'POST /api/presences')
    expect(JSON.parse(options.body)).toEqual({ code: 'K7P2QX', etudiantId: 4 })
  })

  it.each([
    [410, 'CODE_EXPIRE', 'Ce code a expiré.'],
    [409, 'DEJA_PRESENT', 'Votre présence est déjà enregistrée pour cette session.'],
  ])("affiche l'erreur %i %s telle que l'API la renvoie (RG1, RG3)", async (statut, code, message) => {
    connecter()
    simulerApi(routesDeBase({ 'POST /api/presences': [statut, { code, message }] }))
    const utilisateur = userEvent.setup()
    render(<EcranEtudiant />)

    await utilisateur.type(await screen.findByLabelText('Code donné par le formateur'), 'K7P2QX')
    await utilisateur.click(screen.getByRole('button', { name: 'Je suis présent' }))

    expect((await screen.findByRole('alert')).textContent).toBe(message)
    expect(screen.queryByRole('status')).toBeNull()
  })

  it('dépose le lien sur une session ouverte puis recharge « Mes exercices » (EF5)', async () => {
    connecter()
    const fetch = simulerApi(routesDeBase({
      'POST /api/exercices': [201, { id: 21, statut: 'EN_ATTENTE_RELECTURE' }],
    }))
    const utilisateur = userEvent.setup()
    render(<EcranEtudiant />)

    const session = await screen.findByLabelText('Session')
    // Une session clôturée n'est pas proposée au dépôt (RG13).
    expect(within(session).queryByText('Spring Boot — jour 1')).toBeNull()
    await utilisateur.selectOptions(session, '3')
    await utilisateur.type(screen.getByLabelText("Lien de l'exercice"), 'https://github.com/fotso/tp2')
    await utilisateur.click(screen.getByRole('button', { name: 'Déposer' }))

    expect((await screen.findByRole('status')).textContent)
      .toBe('Exercice déposé : des camarades ont été désignés pour le relire.')
    const [[, options]] = appels(fetch, 'POST /api/exercices')
    expect(JSON.parse(options.body)).toEqual({ sessionId: 3, etudiantId: 4, lien: 'https://github.com/fotso/tp2' })
    expect(appels(fetch, 'GET /api/etudiants/4/exercices')).toHaveLength(2)
  })

  it("affiche la note retenue provisoire que l'API renvoie, sans nom de relecteur (EF11, RG23)", async () => {
    connecter()
    simulerApi(routesDeBase({
      'GET /api/etudiants/4/exercices': [200, [{
        id: 21, sessionTitre: 'Spring Boot — jour 2', statut: 'RELU_PARTIELLEMENT',
        noteRetenue: 13, noteProvisoire: true, commentaires: ['Tests clairs.'],
      }]],
    }))
    render(<EcranEtudiant />)

    expect(await screen.findByText('13/20')).toBeTruthy()
    expect(screen.getByText('provisoire — en attente de la seconde relecture')).toBeTruthy()
    expect(screen.getByText('« Tests clairs. »')).toBeTruthy()
  })
})
