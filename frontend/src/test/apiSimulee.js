import { vi } from 'vitest'

// Seul le réseau est simulé : client.js, les hooks et les écrans sont les vrais.
// routes : { 'GET /api/promotions': [200, corps] } ou une fonction (corpsEnvoye) => [statut, corps].
export function simulerApi(routes) {
  const fetch = vi.fn(async (url, options = {}) => {
    const cle = `${options.method ?? 'GET'} ${url}`
    const route = routes[cle]
    if (!route) throw new Error(`Appel non prévu par le test : ${cle}`)
    const corpsEnvoye = options.body ? JSON.parse(options.body) : undefined
    const [statut, corps] = typeof route === 'function' ? route(corpsEnvoye) : route
    return new Response(corps === undefined ? null : JSON.stringify(corps), { status: statut })
  })
  vi.stubGlobal('fetch', fetch)
  return fetch
}

export const appels = (fetch, cle) =>
  fetch.mock.calls.filter(([url, options = {}]) => `${options.method ?? 'GET'} ${url}` === cle)

export const ETUDIANT = { id: 4, nom: 'FOTSO Marie', promotionId: 1 }

export function connecter(etudiant = ETUDIANT) {
  localStorage.setItem('etudiantChoisi', JSON.stringify(etudiant))
}
