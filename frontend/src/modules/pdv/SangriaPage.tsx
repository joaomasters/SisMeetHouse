import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/axios'
import { ArrowDownCircle, ArrowUpCircle, Plus } from 'lucide-react'

interface SangriaItem {
  id: number
  tipo: 'SANGRIA' | 'SUPRIMENTO'
  valor: number
  motivo: string
  createdAt: string
}

interface FechamentoDTO {
  caixaId: number
  valorAbertura: number
  totalSangria: number
  totalSuprimento: number
  totalVendas: number
  totalDinheiro: number
  totalCredito: number
  totalDebito: number
  totalPix: number
  totalFiado: number
  saldoEsperado: number
  quantidadeVendas: number
  movimentos: SangriaItem[]
}

const fmt = (v: number) => `R$ ${(v ?? 0).toFixed(2).replace('.', ',')}`

export default function SangriaPage() {
  const qc = useQueryClient()
  const [caixaId, setCaixaId] = useState<string>('')
  const [tipo, setTipo] = useState<'SANGRIA' | 'SUPRIMENTO'>('SANGRIA')
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')
  const [showFechamento, setShowFechamento] = useState(false)
  const [fechamento, setFechamento] = useState<FechamentoDTO | null>(null)

  const movQuery = useQuery<SangriaItem[]>({
    queryKey: ['sangria', caixaId],
    queryFn: () => api.get(`/pdv/caixa/${caixaId}/movimentos`).then(r => r.data),
    enabled: !!caixaId,
  })

  const registrar = useMutation({
    mutationFn: () => api.post(`/pdv/caixa/${caixaId}/${tipo.toLowerCase()}`, {
      valor: parseFloat(valor.replace(',', '.')),
      motivo,
      operadorId: 1,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['sangria', caixaId] })
      setValor('')
      setMotivo('')
    },
  })

  const verFechamento = async () => {
    const r = await api.get(`/pdv/caixa/${caixaId}/fechamento`)
    setFechamento(r.data)
    setShowFechamento(true)
  }

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Sangria / Suprimento</h1>
          <p className="text-sm text-gray-500">Movimentações de numerário no caixa</p>
        </div>
      </div>

      {/* Identificação do caixa */}
      <div className="bg-white rounded-xl border p-4 flex gap-3 items-end">
        <div className="flex-1">
          <label className="text-xs text-gray-500 font-medium">ID do Caixa</label>
          <input
            type="number"
            value={caixaId}
            onChange={e => setCaixaId(e.target.value)}
            className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm"
            placeholder="Ex: 1"
          />
        </div>
        {caixaId && (
          <button
            onClick={verFechamento}
            className="px-4 py-2 text-sm bg-gray-800 text-white rounded-lg hover:bg-gray-700"
          >
            Ver Fechamento
          </button>
        )}
      </div>

      {/* Formulário */}
      {caixaId && (
        <div className="bg-white rounded-xl border p-5 space-y-4">
          <div className="flex gap-3">
            {(['SANGRIA', 'SUPRIMENTO'] as const).map(t => (
              <button
                key={t}
                onClick={() => setTipo(t)}
                className={`flex-1 py-2.5 rounded-lg text-sm font-medium flex items-center justify-center gap-2 border transition
                  ${tipo === t
                    ? t === 'SANGRIA' ? 'bg-red-600 text-white border-red-600' : 'bg-green-600 text-white border-green-600'
                    : 'text-gray-600 border-gray-200 hover:bg-gray-50'}`}
              >
                {t === 'SANGRIA' ? <ArrowDownCircle size={16} /> : <ArrowUpCircle size={16} />}
                {t}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-500 font-medium">Valor (R$)</label>
              <input
                type="text"
                value={valor}
                onChange={e => setValor(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm"
                placeholder="0,00"
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 font-medium">Motivo</label>
              <input
                type="text"
                value={motivo}
                onChange={e => setMotivo(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm"
                placeholder="Opcional"
              />
            </div>
          </div>

          <button
            onClick={() => registrar.mutate()}
            disabled={!valor || registrar.isPending}
            className="w-full py-2.5 bg-red-600 text-white rounded-lg text-sm font-medium flex items-center justify-center gap-2 hover:bg-red-700 disabled:opacity-50"
          >
            <Plus size={16} />
            Registrar {tipo === 'SANGRIA' ? 'Sangria' : 'Suprimento'}
          </button>
        </div>
      )}

      {/* Lista de movimentos */}
      {movQuery.data && movQuery.data.length > 0 && (
        <div className="bg-white rounded-xl border overflow-hidden">
          <div className="px-5 py-3 border-b bg-gray-50 text-sm font-medium text-gray-600">
            Movimentos do Caixa
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs text-gray-400 border-b">
                <th className="px-4 py-2 text-left">Tipo</th>
                <th className="px-4 py-2 text-left">Valor</th>
                <th className="px-4 py-2 text-left">Motivo</th>
                <th className="px-4 py-2 text-left">Horário</th>
              </tr>
            </thead>
            <tbody>
              {movQuery.data.map(m => (
                <tr key={m.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium
                      ${m.tipo === 'SANGRIA' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                      {m.tipo === 'SANGRIA' ? <ArrowDownCircle size={12} /> : <ArrowUpCircle size={12} />}
                      {m.tipo}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-medium">{fmt(m.valor)}</td>
                  <td className="px-4 py-3 text-gray-500">{m.motivo || '—'}</td>
                  <td className="px-4 py-3 text-gray-400">
                    {new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Fechamento */}
      {showFechamento && fechamento && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4">
            <h2 className="text-lg font-bold">Fechamento do Caixa #{fechamento.caixaId}</h2>
            <div className="space-y-2 text-sm">
              {[
                ['Abertura', fechamento.valorAbertura],
                ['Vendas', fechamento.totalVendas],
                ['Dinheiro', fechamento.totalDinheiro],
                ['Crédito', fechamento.totalCredito],
                ['Débito', fechamento.totalDebito],
                ['PIX', fechamento.totalPix],
                ['Fiado', fechamento.totalFiado],
                ['Suprimento (+)', fechamento.totalSuprimento],
                ['Sangria (-)', fechamento.totalSangria],
              ].map(([label, val]) => (
                <div key={label as string} className="flex justify-between border-b pb-1">
                  <span className="text-gray-500">{label}</span>
                  <span className="font-medium">{fmt(val as number)}</span>
                </div>
              ))}
              <div className="flex justify-between pt-2 text-base font-bold">
                <span>Saldo Esperado em Caixa</span>
                <span className="text-green-600">{fmt(fechamento.saldoEsperado)}</span>
              </div>
              <p className="text-xs text-gray-400">{fechamento.quantidadeVendas} vendas no período</p>
            </div>
            <button
              onClick={() => setShowFechamento(false)}
              className="w-full py-2 bg-gray-800 text-white rounded-lg text-sm"
            >
              Fechar
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
