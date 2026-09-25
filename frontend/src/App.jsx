import { Link, Route, Routes } from 'react-router-dom'
import EcranEtudiant from './ecrans/EcranEtudiant.jsx'
import EcranFormateur from './ecrans/EcranFormateur.jsx'
import EcranRelecteur from './ecrans/EcranRelecteur.jsx'

function Accueil() {
  return (
    <section>
      <h2>Qui êtes-vous ?</h2>
      <ul className="choix">
        <li><Link to="/formateur">Formateur — ouvrir une session, voir le tableau</Link></li>
        <li><Link to="/etudiant">Étudiant — marquer ma présence, déposer mon exercice</Link></li>
        <li><Link to="/relecteur">Relecteur — rendre une relecture</Link></li>
      </ul>
    </section>
  )
}

export default function App() {
  return (
    <div className="page">
      <header>
        <h1><Link to="/">Présence & Relecture</Link></h1>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Accueil />} />
          <Route path="/formateur" element={<EcranFormateur />} />
          <Route path="/etudiant" element={<EcranEtudiant />} />
          <Route path="/relecteur" element={<EcranRelecteur />} />
          <Route path="*" element={<p>Page introuvable. <Link to="/">Retour à l'accueil</Link></p>} />
        </Routes>
      </main>
    </div>
  )
}
