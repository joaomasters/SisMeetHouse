import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CreditCard, CheckCircle } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { ContasAReceber } from '@/types/venda'

const brl = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const statusCor: Record<string, string> = {
  ABERTO:    'bg-red-100 text-red-700',
  PARCIAL:   'bg-yellow-100 text-yellow-700',
  PAGO:      'bg-emerald-100 text-emerald-700',
  CANCELADO: 'bg-gray-100 text-gray-500',
}

export default function ContasReceberPage() {
  const qc = useQueryClient()
  const [clienteId, setClienteId] = useState('1')
  const [pagarId, setPagarId]     = useState<number | null>(null)
  const [valorPag, setValorPag]   = useState('')

  const { data: contas = [], isLoading } = useQuery<ContasAReceber[]>({
    queryKey: ['contas', clienteId],
    queryFn: () =>
      api.get(`/financeiro/contas-receber/cliente/${clienteId}`).then(r => r.data),
    enabled: !!clienteId,
  })

  const pagar = useMutation({
    mutationFn: ({ contaId, valor }: { contaId: number; valor: number }) =>
      api.post(`/financeiro/contas-receber/${contaId}/pagar`, null, { params: { valor } }),
    onSuccess: () => {
      toast.success('Pagamento registrado!')
      qc.invalidateQueries({ queryKey: ['contas'] })
      setPagarId(null)
      setValorPag('')
    },
  })

  const abertas = contas.filter(c => c.status !== 'PAGO' && c.status !== 'CANCELADO')
  const totalAberto = abertas.reduce((s, c) => s + c.valor - c.valorPago, 0)

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CreditCard size={24} className="text-red-600" /> Contas a Receber
          </h1>
          <p className="text-gray-500 text-sm">Fiado, caderneta e faturamento</p>
        </div>
      </div>

      {/* Filtro por cliente */}
      <div className="flex items-center gap-3 mb-4">
        <label className="text-sm font-medium text-gray-600">Cliente ID:</label>
        <input
          type="number"
          value={clienteId}
          onChange={e => setClienteId(e.target.value)}
          className="w-24 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none"
        />
        {totalAberto > 0 && (
          <span className="ml-auto text-sm font-medium text-red-600">
            Total em aberto: <strong className="tabular-nums">{brl(totalAberto)}</strong>
          </span>
        )}
      </div>

      <div className="bg-white rounded-xl shadow overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Carregando...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Descrição</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Valor</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Pago</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Saldo</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Status</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Venc.</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {contas.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">{c.descricao ?? '—'}</td>
                  <td className="px-4 py-3 text-right tabular-nums">{brl(c.valor)}</td>
                  <td className="px-4 py-3 text-right tabular-nums text-emerald-600">{brl(c.valorPago)}</td>
                  <td className="px-4 py-3 text-right tabular-nums font-bold text-red-600">
                    {brl(c.valor - c.valorPago)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${statusCor[c.status]}`}>
                      {c.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500">{c.dataVencimento ?? '—'}</td>
                  <td className="px-4 py-3">
                    {c.status !== 'PAGO' && c.status !== 'CANCELADO' && (
                      <button
                        onClick={() => setPagarId(c.id)}
                        className="p-1.5 rounded hover:bg-emerald-50 text-gray-400 hover:text-emerald-600"
                      >
                        <CheckCircle size={16} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal pagamento */}
      {pagarId && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-sm shadow-xl p-6 space-y-4">
            <h2 className="font-bold text-gray-900">Registrar Pagamento</h2>
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
              <button onClick={() => setPagarId(null)}
                className="flex-1 py-2.5 border rounded-lg text-sm">Cancelar</button>
              <button
                onClick={() => pagar.mutate({ contaId: pagarId, valor: parseFloat(valorPag) })}
                disabled={!valorPag || pagar.isPending}
                className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
              >
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
