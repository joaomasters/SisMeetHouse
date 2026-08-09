import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { BarChart2, TrendingUp, TrendingDown } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { api } from '@/shared/api/axios'

interface DreDTO {
  periodo: string
  receitaBruta: number
  cmv: number
  lucroBruto: number
  percentualLucroBruto: number
  custosOperacionais: number
  lucroLiquido: number
  percentualLucroLiquido: number
  margensPorProduto: {
    produtoId: number
    nomeProduto: string
    quantidadeVendida: number
    receita: number
    cmv: number
    margem: number
    percentualMargem: number
  }[]
}

const brl  = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const pct  = (v: number) => `${v.toFixed(1)}%`

export default function DrePage() {
  const agora = new Date()
  const [ano, setAno]   = useState(agora.getFullYear())
  const [mes, setMes]   = useState(agora.getMonth() + 1)
  const [opex, setOpex] = useState('0')

  const { data: dre, isLoading, refetch } = useQuery<DreDTO>({
    queryKey: ['dre', ano, mes, opex],
    queryFn: () =>
      api.get('/financeiro/dre', { params: { ano, mes, custosOperacionais: opex } })
         .then(r => r.data),
  })

  const cards = dre ? [
    { label: 'Receita Bruta',        value: dre.receitaBruta,        cor: 'text-blue-600',    bg: 'bg-blue-50' },
    { label: 'CMV',                   value: dre.cmv,                 cor: 'text-red-600',     bg: 'bg-red-50',  neg: true },
    { label: 'Lucro Bruto',           value: dre.lucroBruto,          cor: 'text-emerald-600', bg: 'bg-emerald-50', pct: dre.percentualLucroBruto },
    { label: 'Custos Operacionais',   value: dre.custosOperacionais,  cor: 'text-orange-600',  bg: 'bg-orange-50', neg: true },
    { label: 'Lucro Líquido',         value: dre.lucroLiquido,        cor: dre.lucroLiquido >= 0 ? 'text-emerald-700' : 'text-red-700', bg: dre.lucroLiquido >= 0 ? 'bg-emerald-100' : 'bg-red-100', pct: dre.percentualLucroLiquido },
  ] : []

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <BarChart2 size={24} className="text-red-600" /> DRE Simplificado
        </h1>
        <p className="text-gray-500 text-sm">Demonstrativo de Resultado do Exercício</p>
      </div>

      {/* Filtros */}
      <div className="flex items-center gap-4 mb-6 bg-white rounded-xl shadow p-4">
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Mês</label>
          <select value={mes} onChange={e => setMes(Number(e.target.value))}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none">
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i+1} value={i+1}>
                {new Date(2000, i).toLocaleString('pt-BR', { month: 'long' })}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Ano</label>
          <input type="number" value={ano} onChange={e => setAno(Number(e.target.value))}
            className="w-24 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none" />
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Custos Op. (R$)</label>
          <input type="number" step="0.01" value={opex} onChange={e => setOpex(e.target.value)}
            className="w-32 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none" />
        </div>
        <button onClick={() => refetch()}
          className="mt-4 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium">
          Calcular
        </button>
      </div>

      {isLoading && <div className="text-center py-12 text-gray-500">Calculando...</div>}

      {dre && (
        <>
          {/* Cards DRE */}
          <div className="grid grid-cols-5 gap-4 mb-6">
            {cards.map((c, i) => (
              <div key={i} className={`rounded-xl p-4 ${c.bg}`}>
                <p className="text-xs font-medium text-gray-500 mb-1">{c.label}</p>
                <p className={`text-lg font-bold tabular-nums ${c.cor}`}>
                  {brl(c.value)}
                </p>
                {c.pct !== undefined && (
                  <p className={`text-xs mt-0.5 flex items-center gap-1 ${c.cor}`}>
                    {c.value >= 0 ? <TrendingUp size={11} /> : <TrendingDown size={11} />}
                    {pct(c.pct)} da receita
                  </p>
                )}
              </div>
            ))}
          </div>

          {/* Gráfico de margem por produto */}
          <div className="bg-white rounded-xl shadow p-5">
            <h2 className="font-semibold text-gray-900 mb-4">Margem de Lucro por Corte</h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dre.margensPorProduto} layout="vertical"
                margin={{ top: 0, right: 20, left: 120, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tickFormatter={v => `${v.toFixed(0)}%`} domain={[0, 100]} />
                <YAxis type="category" dataKey="nomeProduto" tick={{ fontSize: 12 }} width={120} />
                <Tooltip formatter={(v: number) => `${v.toFixed(1)}%`} />
                <Bar dataKey="percentualMargem" radius={[0, 4, 4, 0]}>
                  {dre.margensPorProduto.map((entry, i) => (
                    <Cell
                      key={i}
                      fill={
                        entry.percentualMargem >= 30 ? '#10b981'
                        : entry.percentualMargem >= 15 ? '#f59e0b'
                        : '#ef4444'
                      }
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
            <p className="text-xs text-gray-400 mt-2">
              Verde ≥ 30% • Amarelo ≥ 15% • Vermelho &lt; 15%
            </p>
          </div>
        </>
      )}
    </div>
  )
}
