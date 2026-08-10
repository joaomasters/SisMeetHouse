import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/axios'
import { TrendingDown, Plus, AlertCircle } from 'lucide-react'

interface ContaPagar {
  id: number; descricao: string; fornecedor: string; valor: number
  valorPago: number; dataVencimento: string; dataPagamento: string | null
  categoria: string; status: string; observacao: string
}

const fmt = (v: number) => `R$ ${(v ?? 0).toFixed(2).replace('.', ',')}`
const statusColor: Record<string, string> = {
  ABERTO: 'bg-yellow-100 text-yellow-700',
  PAGO: 'bg-green-100 text-green-700',
  PARCIAL: 'bg-blue-100 text-blue-700',
  CANCELADO: 'bg-gray-100 text-gray-400',
}

export default function ContasPagarPage() {
  const qc = useQueryClient()
  const [statusFiltro, setStatusFiltro] = useState('ABERTO')
  const [showForm, setShowForm] = useState(false)
  const [pagandoId, setPagandoId] = useState<number | null>(null)
  const [valorPag, setValorPag] = useState('')
  const [form, setForm] = useState({
    descricao: '', fornecedor: '', valor: '', dataVencimento: '', categoria: '', observacao: ''
  })

  const contas = useQuery<ContaPagar[]>({
    queryKey: ['contas-pagar', statusFiltro],
    queryFn: () => api.get(`/financeiro/contas-pagar?status=${statusFiltro}`).then(r => r.data),
  })

  const vencidas = useQuery<ContaPagar[]>({
    queryKey: ['contas-pagar-vencidas'],
    queryFn: () => api.get('/financeiro/contas-pagar/vencidas').then(r => r.data),
  })

  const criar = useMutation({
    mutationFn: () => api.post('/financeiro/contas-pagar', {
      ...form, valor: parseFloat(form.valor.replace(',', '.')),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['contas-pagar'] })
      setShowForm(false)
      setForm({ descricao: '', fornecedor: '', valor: '', dataVencimento: '', categoria: '', observacao: '' })
    },
  })

  const pagar = useMutation({
    mutationFn: () => api.post(`/financeiro/contas-pagar/${pagandoId}/pagar`, {
      valor: parseFloat(valorPag.replace(',', '.')),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['contas-pagar'] })
      qc.invalidateQueries({ queryKey: ['contas-pagar-vencidas'] })
      setPagandoId(null)
      setValorPag('')
    },
  })

  const cancelar = useMutation({
    mutationFn: (id: number) => api.post(`/financeiro/contas-pagar/${id}/cancelar`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['contas-pagar'] }),
  })

  const totalAberto = contas.data?.reduce((s, c) => s + (c.valor - c.valorPago), 0) ?? 0

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <TrendingDown size={24} className="text-red-500" />
            Contas a Pagar
          </h1>
          <p className="text-sm text-gray-500">Gestão de pagamentos e fornecedores</p>
        </div>
        <button onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700">
          <Plus size={16} /> Nova Conta
        </button>
      </div>

      {/* Alerta de vencidas */}
      {(vencidas.data?.length ?? 0) > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3">
          <AlertCircle size={20} className="text-red-500 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-sm font-semibold text-red-700">
              {vencidas.data?.length} conta(s) vencida(s) sem pagamento
            </p>
            <p className="text-xs text-red-500 mt-0.5">
              Total: {fmt(vencidas.data?.reduce((s, c) => s + c.valor, 0) ?? 0)}
            </p>
          </div>
        </div>
      )}

      {/* Filtros + total */}
      <div className="bg-white rounded-xl border p-4 flex items-center gap-3">
        {['ABERTO', 'PARCIAL', 'PAGO', 'CANCELADO'].map(s => (
          <button key={s} onClick={() => setStatusFiltro(s)}
            className={`px-4 py-1.5 rounded-full text-xs font-medium transition
              ${statusFiltro === s ? 'bg-gray-800 text-white' : 'border text-gray-500 hover:bg-gray-50'}`}>
            {s}
          </button>
        ))}
        <div className="ml-auto text-right">
          <p className="text-xs text-gray-400">Saldo devedor</p>
          <p className="text-lg font-bold text-red-600">{fmt(totalAberto)}</p>
        </div>
      </div>

      {/* Tabela */}
      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b text-xs text-gray-500 uppercase tracking-wide">
              <th className="px-4 py-3 text-left">Descrição</th>
              <th className="px-4 py-3 text-left">Fornecedor</th>
              <th className="px-4 py-3 text-left">Vencimento</th>
              <th className="px-4 py-3 text-right">Valor</th>
              <th className="px-4 py-3 text-right">Pago</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {contas.data?.length === 0 && (
              <tr><td colSpan={7} className="text-center py-10 text-gray-400">Nenhuma conta encontrada</td></tr>
            )}
            {contas.data?.map(c => (
              <tr key={c.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{c.descricao}</td>
                <td className="px-4 py-3 text-gray-500">{c.fornecedor || '—'}</td>
                <td className="px-4 py-3">
                  <span className={new Date(c.dataVencimento) < new Date() && c.status === 'ABERTO'
                    ? 'text-red-600 font-medium' : 'text-gray-600'}>
                    {new Date(c.dataVencimento).toLocaleDateString('pt-BR')}
                  </span>
                </td>
                <td className="px-4 py-3 text-right font-medium">{fmt(c.valor)}</td>
                <td className="px-4 py-3 text-right text-green-600">{fmt(c.valorPago)}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[c.status]}`}>
                    {c.status}
                  </span>
                </td>
                <td className="px-4 py-3">
                  {(c.status === 'ABERTO' || c.status === 'PARCIAL') && (
                    <div className="flex gap-2">
                      <button onClick={() => { setPagandoId(c.id); setValorPag('') }}
                        className="text-xs px-2 py-1 bg-green-600 text-white rounded hover:bg-green-700">
                        Pagar
                      </button>
                      <button onClick={() => cancelar.mutate(c.id)}
                        className="text-xs px-2 py-1 border text-gray-500 rounded hover:bg-gray-50">
                        Cancelar
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal nova conta */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4">
            <h2 className="text-lg font-bold">Nova Conta a Pagar</h2>
            {[
              { label: 'Descrição*', key: 'descricao', type: 'text', placeholder: 'Ex: Compra de bovino' },
              { label: 'Fornecedor', key: 'fornecedor', type: 'text', placeholder: 'Nome do fornecedor' },
              { label: 'Valor (R$)*', key: 'valor', type: 'text', placeholder: '0,00' },
              { label: 'Vencimento*', key: 'dataVencimento', type: 'date', placeholder: '' },
              { label: 'Categoria', key: 'categoria', type: 'text', placeholder: 'Ex: Matéria-prima' },
            ].map(f => (
              <div key={f.key}>
                <label className="text-xs text-gray-500 font-medium">{f.label}</label>
                <input type={f.type} value={(form as any)[f.key]}
                  onChange={e => setForm(p => ({ ...p, [f.key]: e.target.value }))}
                  placeholder={f.placeholder}
                  className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
            ))}
            <div className="flex gap-3">
              <button onClick={() => setShowForm(false)}
                className="flex-1 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50">
                Cancelar
              </button>
              <button onClick={() => criar.mutate()}
                disabled={!form.descricao || !form.valor || !form.dataVencimento || criar.isPending}
                className="flex-1 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50">
                {criar.isPending ? 'Salvando...' : 'Criar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal pagar */}
      {pagandoId !== null && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-xs p-6 space-y-4">
            <h2 className="text-lg font-bold">Registrar Pagamento</h2>
            <div>
              <label className="text-xs text-gray-500 font-medium">Valor Pago (R$)</label>
              <input type="text" value={valorPag} onChange={e => setValorPag(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm" placeholder="0,00" autoFocus />
            </div>
            <div className="flex gap-3">
              <button onClick={() => setPagandoId(null)}
                className="flex-1 py-2 border rounded-lg text-sm">Cancelar</button>
              <button onClick={() => pagar.mutate()}
                disabled={!valorPag || pagar.isPending}
                className="flex-1 py-2 bg-green-600 text-white rounded-lg text-sm font-medium disabled:opacity-50">
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
