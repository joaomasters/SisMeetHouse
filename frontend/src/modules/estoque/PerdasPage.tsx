import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/axios'
import { AlertTriangle, Plus } from 'lucide-react'

interface Produto { id: number; nome: string; unidadeMedida: string; precoCusto: number }
interface Perda {
  id: number
  produto: { nome: string; unidadeMedida: string }
  quantidade: number
  custoTotal: number
  motivo: string
  observacao: string
  createdAt: string
}

const MOTIVOS = ['VENCIMENTO', 'AVARIA', 'FURTO', 'DESOSSA', 'OUTROS']
const fmt = (v: number) => `R$ ${(v ?? 0).toFixed(2).replace('.', ',')}`

const hoje = new Date().toISOString().slice(0, 10)

export default function PerdasPage() {
  const qc = useQueryClient()
  const [inicio, setInicio] = useState(hoje)
  const [fim, setFim] = useState(hoje)
  const [showForm, setShowForm] = useState(false)
  const [produtoId, setProdutoId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [motivo, setMotivo] = useState('VENCIMENTO')
  const [observacao, setObservacao] = useState('')

  const produtos = useQuery<Produto[]>({
    queryKey: ['produtos'],
    queryFn: () => api.get('/estoque/produtos').then(r => r.data),
  })

  const perdas = useQuery<Perda[]>({
    queryKey: ['perdas', inicio, fim],
    queryFn: () => api.get(`/estoque/perdas?inicio=${inicio}&fim=${fim}`).then(r => r.data),
  })

  const lancar = useMutation({
    mutationFn: () => api.post('/estoque/perdas', {
      produtoId: parseInt(produtoId),
      quantidade: parseFloat(quantidade.replace(',', '.')),
      motivo,
      observacao,
      usuarioId: 1,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['perdas'] })
      setShowForm(false)
      setProdutoId('')
      setQuantidade('')
      setObservacao('')
    },
  })

  const totalPerdas = perdas.data?.reduce((s, p) => s + (p.custoTotal ?? 0), 0) ?? 0

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <AlertTriangle className="text-orange-500" size={24} />
            Controle de Perdas
          </h1>
          <p className="text-sm text-gray-500">Vencimentos, avarias, furtos e quebras</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700"
        >
          <Plus size={16} /> Lançar Perda
        </button>
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
        <div className="ml-auto text-right">
          <p className="text-xs text-gray-400">Total de perdas no período</p>
          <p className="text-xl font-bold text-red-600">{fmt(totalPerdas)}</p>
        </div>
      </div>

      {/* Tabela */}
      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b text-xs text-gray-500 uppercase tracking-wide">
              <th className="px-4 py-3 text-left">Produto</th>
              <th className="px-4 py-3 text-left">Motivo</th>
              <th className="px-4 py-3 text-right">Qtd</th>
              <th className="px-4 py-3 text-right">Custo</th>
              <th className="px-4 py-3 text-left">Observação</th>
              <th className="px-4 py-3 text-left">Data</th>
            </tr>
          </thead>
          <tbody>
            {perdas.data?.length === 0 && (
              <tr><td colSpan={6} className="text-center py-10 text-gray-400">Nenhuma perda no período</td></tr>
            )}
            {perdas.data?.map(p => (
              <tr key={p.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{p.produto.nome}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium
                    ${p.motivo === 'FURTO' ? 'bg-purple-100 text-purple-700'
                      : p.motivo === 'VENCIMENTO' ? 'bg-red-100 text-red-700'
                      : p.motivo === 'AVARIA' ? 'bg-orange-100 text-orange-700'
                      : 'bg-gray-100 text-gray-600'}`}>
                    {p.motivo}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">{p.quantidade} {p.produto.unidadeMedida}</td>
                <td className="px-4 py-3 text-right font-medium text-red-600">{fmt(p.custoTotal)}</td>
                <td className="px-4 py-3 text-gray-500 text-xs">{p.observacao || '—'}</td>
                <td className="px-4 py-3 text-gray-400">
                  {new Date(p.createdAt).toLocaleDateString('pt-BR')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal lançar perda */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4">
            <h2 className="text-lg font-bold">Lançar Perda</h2>
            <div>
              <label className="text-xs text-gray-500 font-medium">Produto</label>
              <select value={produtoId} onChange={e => setProdutoId(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm">
                <option value="">Selecione...</option>
                {produtos.data?.map(p => (
                  <option key={p.id} value={p.id}>{p.nome}</option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-gray-500 font-medium">Quantidade</label>
                <input type="text" value={quantidade} onChange={e => setQuantidade(e.target.value)}
                  className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm" placeholder="0,000" />
              </div>
              <div>
                <label className="text-xs text-gray-500 font-medium">Motivo</label>
                <select value={motivo} onChange={e => setMotivo(e.target.value)}
                  className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm">
                  {MOTIVOS.map(m => <option key={m}>{m}</option>)}
                </select>
              </div>
            </div>
            <div>
              <label className="text-xs text-gray-500 font-medium">Observação</label>
              <input type="text" value={observacao} onChange={e => setObservacao(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm" />
            </div>
            <div className="flex gap-3">
              <button onClick={() => setShowForm(false)}
                className="flex-1 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50">
                Cancelar
              </button>
              <button onClick={() => lancar.mutate()}
                disabled={!produtoId || !quantidade || lancar.isPending}
                className="flex-1 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50">
                {lancar.isPending ? 'Salvando...' : 'Lançar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
