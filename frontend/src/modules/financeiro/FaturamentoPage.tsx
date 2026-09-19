import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { DollarSign, Plus, CheckCircle } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { FaturamentoCliente, Cliente } from '@/types/venda'

const brl = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const statusCor: Record<string, string> = {
  ABERTO:  'bg-blue-100 text-blue-700',
  PARCIAL: 'bg-yellow-100 text-yellow-700',
  QUITADO: 'bg-emerald-100 text-emerald-700',
  VENCIDO: 'bg-red-100 text-red-700',
}

export default function FaturamentoPage() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [clienteId, setClienteId] = useState('')
  const [inicio, setInicio]       = useState('')
  const [fim, setFim]             = useState('')

  // Pagamento de um fechamento — antes só existia em Contas a Receber.
  const [pagarFat, setPagarFat]   = useState<FaturamentoCliente | null>(null)
  const [valorPag, setValorPag]   = useState('')

  const { data: faturamentos = [], isLoading } = useQuery<FaturamentoCliente[]>({
    queryKey: ['faturamentos'],
    queryFn: () => api.get('/financeiro/faturamento/abertos').then(r => r.data),
  })

  const { data: clientes = [] } = useQuery<Cliente[]>({
    queryKey: ['clientes'],
    queryFn: () => api.get('/financeiro/clientes').then(r => r.data).catch(() => []),
  })

  const gerar = useMutation({
    mutationFn: () =>
      api.post('/financeiro/faturamento/fechar', null, {
        params: { clienteId, inicio, fim },
      }),
    onSuccess: () => {
      toast.success('Faturamento gerado!')
      qc.invalidateQueries({ queryKey: ['faturamentos'] })
      setShowForm(false)
    },
  })

  const pagar = useMutation({
    mutationFn: ({ faturamentoId, valor }: { faturamentoId: number; valor: number }) =>
      api.post(`/financeiro/faturamento/${faturamentoId}/pagar`, null, { params: { valor } }),
    onSuccess: () => {
      toast.success('Pagamento registrado!')
      qc.invalidateQueries({ queryKey: ['faturamentos'] })
      setPagarFat(null)
      setValorPag('')
    },
  })

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <DollarSign size={24} className="text-red-600" /> Faturamento
          </h1>
          <p className="text-gray-500 text-sm">Fechamento de contas para clientes atacado/conveniados</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium"
        >
          <Plus size={16} /> Gerar Fechamento
        </button>
      </div>

      {/* Tabela */}
      <div className="bg-white rounded-xl shadow overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Carregando...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Cliente</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Período</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Total</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Pago</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Saldo</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Status</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Vencimento</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {faturamentos.map(f => (
                <tr key={f.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{f.cliente.nome}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">
                    {f.periodoInicio} a {f.periodoFim}
                  </td>
                  <td className="px-4 py-3 text-right tabular-nums">{brl(f.totalVendas)}</td>
                  <td className="px-4 py-3 text-right tabular-nums text-emerald-600">{brl(f.totalPago)}</td>
                  <td className="px-4 py-3 text-right tabular-nums font-bold text-red-600">{brl(f.saldoDevedor)}</td>
                  <td className="px-4 py-3 text-center">
                    <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${statusCor[f.status]}`}>
                      {f.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{f.dataVencimento}</td>
                  <td className="px-4 py-3">
                    {f.status !== 'QUITADO' && (
                      <button
                        onClick={() => setPagarFat(f)}
                        title="Registrar pagamento"
                        className="flex items-center gap-1 px-2 py-1 text-xs bg-emerald-50 text-emerald-700 rounded-lg hover:bg-emerald-100 font-medium"
                      >
                        <CheckCircle size={13} /> Receber
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal gerar fechamento */}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl p-6 space-y-4">
            <h2 className="font-bold text-gray-900">Gerar Fechamento de Faturamento</h2>

            <div>
              <label className="text-xs font-medium text-gray-600 block mb-1">Cliente</label>
              <select
                value={clienteId}
                onChange={e => setClienteId(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none"
              >
                <option value="">Selecione um cliente...</option>
                {clientes.map(c => (
                  <option key={c.id} value={c.id}>{c.nome}</option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Data Início</label>
                <input type="date" value={inicio} onChange={e => setInicio(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Data Fim</label>
                <input type="date" value={fim} onChange={e => setFim(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none" />
              </div>
            </div>

            <div className="flex gap-3">
              <button onClick={() => setShowForm(false)}
                className="flex-1 py-2.5 border rounded-lg text-sm hover:bg-gray-50">
                Cancelar
              </button>
              <button
                onClick={() => gerar.mutate()}
                disabled={!clienteId || !inicio || !fim || gerar.isPending}
                className="flex-1 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
              >
                {gerar.isPending ? 'Gerando...' : 'Gerar'}
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Modal registrar pagamento */}
      {pagarFat && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-sm shadow-xl p-6 space-y-4">
            <h2 className="font-bold text-gray-900">Registrar Pagamento</h2>
            <p className="text-sm text-gray-500">
              {pagarFat.cliente.nome} — saldo devedor <strong className="text-red-600">{brl(pagarFat.saldoDevedor)}</strong>
            </p>
            <div>
              <label className="text-xs font-medium text-gray-600 block mb-1">Valor Recebido (R$)</label>
              <input
                type="number" step="0.01"
                value={valorPag}
                onChange={e => setValorPag(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                autoFocus
              />
            </div>
            <div className="flex gap-3">
              <button onClick={() => { setPagarFat(null); setValorPag('') }}
                className="flex-1 py-2.5 border rounded-lg text-sm hover:bg-gray-50">
                Cancelar
              </button>
              <button
                onClick={() => pagar.mutate({ faturamentoId: pagarFat.id, valor: parseFloat(valorPag) })}
                disabled={!valorPag || parseFloat(valorPag) <= 0 || pagar.isPending}
                className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
              >
                {pagar.isPending ? 'Registrando...' : 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}