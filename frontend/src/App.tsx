import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './shared/components/Layout'
import PDVPage from './modules/pdv/PDVPage'
import ProdutosPage from './modules/estoque/ProdutosPage'
import DesossaPage from './modules/estoque/DesossaPage'
import FaturamentoPage from './modules/financeiro/FaturamentoPage'
import ContasReceberPage from './modules/financeiro/ContasReceberPage'
import DrePage from './modules/financeiro/DrePage'
import CargaBalancaPage from './modules/balanca/CargaBalancaPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* PDV fica sem sidebar (tela cheia) */}
        <Route path="/pdv" element={<PDVPage />} />

        {/* Back-office com layout */}
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/estoque/produtos" replace />} />
          <Route path="/estoque/produtos"  element={<ProdutosPage />} />
          <Route path="/estoque/desossa"   element={<DesossaPage />} />
          <Route path="/financeiro/faturamento"    element={<FaturamentoPage />} />
          <Route path="/financeiro/contas-receber" element={<ContasReceberPage />} />
          <Route path="/financeiro/dre"            element={<DrePage />} />
          <Route path="/balanca"           element={<CargaBalancaPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
