import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import api from '../../shared/api/axios'
import { BarChart2, TrendingUp, TrendingDown, Package, ShoppingBag } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

interface RelatorioVendas {
  totalVendas: number; quantidadeVendas: number; ticketMedio: number; totalPerdas: number
  topProdutos: { produtoId: number; nomeProduto: string; quantidadeTotal: number; valorTotal: number }[]
}
interface FluxoCaixa {
  totalRecebimentos: number; totalPagamentos: number; saldo: number
}

const fmt = (v: number) => `R$ ${(v ?? 0).toFixed(2).replace('.', ',')}`
const hoje = new Date().toISOString().slice(0, 10)
const primeiroDiaMes = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10)

export default function RelatoriosPage() {
  const [inicio, setInicio] = useState(primeiroDiaMes)
  const [fim, setFim] = useState(hoje)

  const relVendas = useQuery<RelatorioVendas>({
    queryKey: ['rel-vendas', inicio, fim],
    queryFn: () => api.get(`/financeiro/relatorios/vendas?inicio=${inicio}&fim=${fim}`).then(r => r.data),
  })

  const fluxo = useQuery<FluxoCaixa>({
    queryKey: ['fluxo-caixa', inicio, fim],
    queryFn: () => api.get(`/financeiro/relatorios/fluxo-caixa?inicio=${inicio}&fim=${fim}`).then(r => r.data),
  })

  const cards = [
    { label: 'Total de Vendas', value: fmt(relVendas.data?.totalVendas ?? 0), icon: TrendingUp, color: 'text-green-600 bg-green-50' },
    { label: 'Qtd. Vendas', value: relVendas.data?.quantidadeVendas ?? 0, icon: ShoppingBag, color: 'text-blue-600 bg-blue-50' },
    { label: 'Ticket Médio', value: fmt(relVendas.data?.ticketMedio ?? 0), icon: BarChart2, color: 'text-purple-600 bg-purple-50' },
    { label: 'Total Perdas', value: fmt(relVendas.data?.totalPerdas ?? 0), icon: TrendingDown, color: 'text-red-600 bg-red-50' },
  ]

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Relatórios</h1>
        <p className="text-sm text-gray-500">Análise de desempenho e controle gerencial</p>
      </div>

      {/* Filtro de período */}
      <div className="bg-white rounded-xl border p-4 flex gap-4 items-end">
        <div>
          <label className="text-xs text-gray-500 font-medium">De</label>
          <input type="date" value={inicio} onChange={e => setInicio(e.target.value)}
            className="mt-1 block border rounded-lg px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="text-xs text-gray-500 font-medium">Até</label>
          <input type="date" value={fim} onChange={e => setFim(e.target.value)}
            className="mt-1 block border rounded-lg px-3 py-2 text-sm" />
        </div>
      </div>

      {/* Cards de resumo */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(c => (
          <div key={c.label} className="bg-white rounded-xl border p-5">
            <div className={`inline-flex p-2 rounded-lg ${c.color} mb-3`}>
              <c.icon size={20} />
            </div>
            <p className="text-2xl font-bold text-gray-800">{c.value}</p>
            <p className="text-xs text-gray-500 mt-1">{c.label}</p>
          </div>
        ))}
      </div>

      {/* Fluxo de Caixa */}
      <div className="bg-white rounded-xl border p-5">
        <h2 className="text-base font-semibold text-gray-700 mb-4">Fluxo de Caixa</h2>
        <div className="grid grid-cols-3 gap-4">
          <div className="text-center p-4 bg-green-50 rounded-lg">
            <p className="text-xs text-gray-500">Entradas</p>
            <p className="text-xl font-bold text-green-600">{fmt(fluxo.data?.totalRecebimentos ?? 0)}</p>
          </div>
          <div className="text-center p-4 bg-red-50 rounded-lg">
            <p className="text-xs text-gray-500">Saídas</p>
            <p className="text-xl font-bold text-red-600">{fmt(fluxo.data?.totalPagamentos ?? 0)}</p>
          </div>
          <div className={`text-center p-4 rounded-lg ${(fluxo.data?.saldo ?? 0) >= 0 ? 'bg-blue-50' : 'bg-orange-50'}`}>
            <p className="text-xs text-gray-500">Saldo</p>
            <p className={`text-xl font-bold ${(fluxo.data?.saldo ?? 0) >= 0 ? 'text-blue-600' : 'text-orange-600'}`}>
              {fmt(fluxo.data?.saldo ?? 0)}
            </p>
          </div>
        </div>
      </div>

      {/* Top 10 Produtos */}
      {relVendas.data?.topProdutos && relVendas.data.topProdutos.length > 0 && (
        <div className="bg-white rounded-xl border p-5">
          <h2 className="text-base font-semibold text-gray-700 mb-4 flex items-center gap-2">
            <Package size={18} className="text-gray-500" />
            Top 10 Produtos por Faturamento
          </h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={relVendas.data.topProdutos} layout="vertical"
                margin={{ left: 20, right: 20, top: 5, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tickFormatter={v => `R$${v.toFixed(0)}`} tick={{ fontSize: 11 }} />
                <YAxis type="category" dataKey="nomeProduto" width={120} tick={{ fontSize: 11 }} />
                <Tooltip formatter={(v: number) => fmt(v)} />
                <Bar dataKey="valorTotal" name="Faturamento" fill="#dc2626" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Tabela ranking */}
          <table className="w-full text-sm mt-4">
            <thead>
              <tr className="text-xs text-gray-400 border-b">
                <th className="py-2 text-left">#</th>
                <th className="py-2 text-left">Produto</th>
                <th className="py-2 text-right">Qtd Vendida</th>
                <th className="py-2 text-right">Faturamento</th>
              </tr>
            </thead>
            <tbody>
              {relVendas.data.topProdutos.map((p, i) => (
                <tr key={p.produtoId} className="border-b last:border-0">
                  <td className="py-2 text-gray-400 font-mono">{i + 1}</td>
                  <td className="py-2 font-medium">{p.nomeProduto}</td>
                  <td className="py-2 text-right text-gray-600">{p.quantidadeTotal?.toFixed(3)}</td>
                  <td className="py-2 text-right font-semibold text-green-600">{fmt(p.valorTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
