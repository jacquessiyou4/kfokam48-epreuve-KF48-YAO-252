import { afterEach, describe, expect, it, vi } from 'vitest'
import { ErreurApi, marquerPresence, listerPromotions, cloturerSession } from './client.js'

// F3 — tous les appels passent par la couche API : on remplace fetch et on vérifie ce qu'elle en fait.
function reponse(statut, corps) {
  return Promise.resolve(new Response(corps === undefined ? null : JSON.stringify(corps), { status: statut }))
}

afterEach(() => vi.unstubAllGlobals())

describe('couche API', () => {
  it('renvoie le JSON de la réponse en cas de succès', async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(200, [{ id: 1, nom: 'KF48' }])))

    await expect(listerPromotions()).resolves.toEqual([{ id: 1, nom: 'KF48' }])
    expect(fetch).toHaveBeenCalledWith('/api/promotions', expect.objectContaining({ method: 'GET' }))
  })

  it('envoie le corps en JSON sur un POST', async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(201, { id: 7 })))

    await marquerPresence('ABC123', 4)

    const [chemin, options] = fetch.mock.calls[0]
    expect(chemin).toBe('/api/presences')
    expect(options.method).toBe('POST')
    expect(options.headers).toEqual({ 'Content-Type': 'application/json' })
    expect(JSON.parse(options.body)).toEqual({ code: 'ABC123', etudiantId: 4 })
  })

  it('transforme une erreur { code, message } en ErreurApi avec statut et code (RG1)', async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(410, { code: 'CODE_EXPIRE', message: 'Le code a expiré.' })))

    const erreur = await marquerPresence('ABC123', 4).catch((e) => e)

    expect(erreur).toBeInstanceOf(ErreurApi)
    expect(erreur.statut).toBe(410)
    expect(erreur.code).toBe('CODE_EXPIRE')
    expect(erreur.message).toBe('Le code a expiré.')
  })

  it("garde le statut quand l'erreur n'a pas de corps", async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(404)))

    const erreur = await listerPromotions().catch((e) => e)

    expect(erreur.statut).toBe(404)
    expect(erreur.code).toBe('INCONNUE')
  })

  it('explique un backend arrêté quand nginx répond par une page HTML 502', async () => {
    const pageNginx = '<html>\n<head><title>502 Bad Gateway</title></head>\n</html>'
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(new Response(pageNginx, { status: 502 }))))

    const erreur = await listerPromotions().catch((e) => e)

    expect(erreur).toBeInstanceOf(ErreurApi)
    expect(erreur.code).toBe('SERVEUR_INJOIGNABLE')
    expect(erreur.message).toBe('Serveur injoignable. Réessayez dans un instant.')
  })

  it('explique un backend arrêté quand le relais répond 503 sans corps', async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(503)))

    const erreur = await listerPromotions().catch((e) => e)

    expect(erreur.code).toBe('SERVEUR_INJOIGNABLE')
  })

  it("signale une réponse qui n'est pas du JSON sans exposer l'erreur de parsing", async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(new Response('<!doctype html>', { status: 200 }))))

    const erreur = await listerPromotions().catch((e) => e)

    expect(erreur.code).toBe('REPONSE_INVALIDE')
    expect(erreur.message).not.toMatch(/JSON|token/)
  })

  it('signale une panne réseau par le code RESEAU', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new TypeError('Failed to fetch'))))

    const erreur = await listerPromotions().catch((e) => e)

    expect(erreur).toBeInstanceOf(ErreurApi)
    expect(erreur.code).toBe('RESEAU')
  })

  it('accepte une réponse sans corps', async () => {
    vi.stubGlobal('fetch', vi.fn(() => reponse(204)))

    await expect(cloturerSession(3)).resolves.toBeNull()
  })
})
