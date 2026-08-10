import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/axios'
import { ClipboardList, CheckCircle2, XCircle, ChevronRight } from 'lucide-react'

interface Inventario {
  id: number; status: string; observacao: string
  dataInicio: string; dataFim: string; createdAt: string
}
interface InventarioItem {
  id: number
  produto: { id: number; nome: string; unidadeMedida: string }
  saldoSistema: number; saldoContado: number | null; divergencia: number | null
}

export default function InventarioPage() {
  const qc = useQueryClient()
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [observacao, setObservacao] = useState('')
  const [contagens, setContagens] = useState<Record<number, string>>({})

  const lista = useQuery<Inventario[]>({
    queryKey: ['inventarios'],
    queryFn: () => api.get('/estoque/inventario').then(r => r.data),
  })

  const itens = useQuery<InventarioItem[]>({
    queryKey: ['inventario-itens', selectedId],
    queryFn: () => api.get(`/estoque/inventario/${selectedId}/itens`).then(r => r.data),
    enabled: !!selectedId,
  })

  const abrir = useMutation({
    mutationFn: () => api.post('/estoque/inventario/abrir', { usuarioId: 1, observacao }),
    onSuccess: (r) => {
      qc.invalidateQueries({ queryKey: ['inventarios'] })
      setSelectedId(r.data.id)
    },
  })

  const contar = useMutation({
    mutationFn: ({ produtoId, val }: { produtoId: number; val: string }) =>
      api.patch(`/estoque/inventario/${selectedId}/itens/${produtoId}`, {
        saldoContado: parseFloat(val.replace(',', '.')),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['inventario-itens', selectedId] }),
  })

  const finalizar = useMutation({
    mutationFn: () => api.post(`/estoque/inventario/${selectedId}/finalizar`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['inventarios'] })
      qc.invalidateQueries({ queryKey: ['inventario-itens', selectedId] })
    },
  })

  const cancelar = useMutation({
    mutationFn: () => api.post(`/estoque/inventario/${selectedId}/cancelar`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['inventarios'] }),
  })

  const inventarioAtivo = lista.data?.find(i => i.id === selectedId)
  const isAberto = inventarioAtivo?.status === 'ABERTO'

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <ClipboardList size={24} className="text-blue-600" />
            Inventário Físico
          </h1>
          <p className="text-sm text-gray-500">Contagem física vs saldo do sistema com ajuste automático</p>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-5">
        {/* Painel esquerdo — lista */}
        <div className="col-span-1 space-y-3">
          <div className="bg-white rounded-xl border p-4 space-y-3">
            <p className="text-sm font-semibold text-gray-700">Novo Inventário</p>
            <input type="text" value={observacao} onChange={e => setObservacao(e.target.value)}
              placeholder="Observação (opcional)"
              className="w-full border rounded-lg px-3 py-2 text-sm" />
            <button onClick={() => abrir.mutate()}
              disabled={abrir.isPending}
              className="w-full py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50">
              Abrir Inventário
            </button>
          </div>

          <div className="space-y-2">
            {lista.data?.map(inv => (
              <button key={inv.id} onClick={() => setSelectedId(inv.id)}
                className={`w-full text-left p-3 rounded-xl border text-sm transition
                  ${selectedId === inv.id ? 'border-blue-500 bg-blue-50' : 'bg-white hover:bg-gray-50'}`}>
                <div className="flex items-center justify-between">
                  <span className="font-medium">#{inv.id}</span>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium
                    ${inv.status === 'ABERTO' ? 'bg-green-100 text-green-700'
                      : inv.status === 'FINALIZADO' ? 'bg-blue-100 text-blue-700'
                      : 'bg-gray-100 text-gray-500'}`}>
                    {inv.status}
                  </span>
                </div>
                <p className="text-xs text-gray-400 mt-1">
                  {new Date(inv.createdAt).toLocaleDateString('pt-BR')}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Painel direito — itens */}
        <div className="col-span-2">
          {!selectedId && (
            <div className="h-full flex items-center justify-center text-gray-400 text-sm">
              Selecione um inventário para visualizar os itens
            </div>
          )}
          {selectedId && (
            <div className="bg-white rounded-xl border overflow-hidden">
              <div className="px-5 py-3 border-b bg-gray-50 flex items-center justify-between">
                <span className="text-sm font-semibold text-gray-700">Itens — Inventário #{selectedId}</span>
                {isAberto && (
                  <div className="flex gap-2">
                    <button onClick={() => cancelar.mutate()}
                      className="flex items-center gap-1 px-3 py-1.5 text-xs border text-gray-600 rounded-lg hover:bg-gray-100">
                      <XCircle size={14} /> Cancelar
                    </button>
                    <button onClick={() => finalizar.mutate()}
                      className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                      <CheckCircle2 size={14} /> Finalizar e Ajustar
                    </button>
                  </div>
                )}
              </div>
              <div className="overflow-y-auto max-h-[60vh]">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs text-gray-400 border-b">
                      <th className="px-4 py-2 text-left">Produto</th>
                      <th className="px-4 py-2 text-right">Sistema</th>
                      <th className="px-4 py-2 text-right">Contado</th>
                      <th className="px-4 py-2 text-right">Divergência</th>
                    </tr>
                  </thead>
                  <tbody>
                    {itens.data?.map(item => (
                      <tr key={item.id} className="border-b last:border-0 hover:bg-gray-50">
                        <td className="px-4 py-2.5 font-medium">{item.produto.nome}</td>
                        <td className="px-4 py-2.5 text-right text-gray-500">
                          {item.saldoSistema?.toFixed(3)} {item.produto.unidadeMedida}
                        </td>
                        <td className="px-4 py-2.5 text-right">
                          {isAberto ? (
                            <input
                              type="text"
                              value={contagens[item.produto.id] ?? item.saldoContado?.toFixed(3) ?? ''}
                              onChange={e => setContagens(p => ({ ...p, [item.produto.id]: e.target.value }))}
                              onBlur={() => {
                                const v = contagens[item.produto.id]
                                if (v !== undefined) contar.mutate({ produtoId: item.produto.id, val: v })
                              }}
                              className="w-24 border rounded px-2 py-1 text-right text-sm"
                            />
                          ) : (
                            <span>{item.saldoContado?.toFixed(3) ?? '—'}</span>
                          )}
                        </td>
                        <td className="px-4 py-2.5 text-right font-medium">
                          {item.divergencia != null ? (
                            <span className={item.divergencia >= 0 ? 'text-green-600' : 'text-red-600'}>
                              {item.divergencia >= 0 ? '+' : ''}{item.divergencia.toFixed(3)}
                            </span>
                          ) : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
