import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, AlertTriangle, Pencil, Trash2 } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Produto } from '@/types/produto'
import ProdutoForm from './components/ProdutoForm'

const brl  = (v?: number) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const qtd3 = (v: number)  => v.toLocaleString('pt-BR', { minimumFractionDigits: 3 })

export default function ProdutosPage() {
  const qc      = useQueryClient()
  const [busca, setBusca]   = useState('')
  const [form, setForm]     = useState<Partial<Produto> | null>(null)

  const { data: produtos = [], isLoading, isError, error } = useQuery<Produto[]>({
    queryKey: ['produtos', busca],
    queryFn: () =>
      api.get('/estoque/produtos', { params: busca ? { nome: busca } : {} })
         .then(r => r.data),
  })

  const deletar = useMutation({
    mutationFn: (id: number) => api.delete(`/estoque/produtos/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['produtos'] })
      toast.success('Produto inativado')
    },
  })

  return (
    <div className="p-6">
      {/* Cabeçalho */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Produtos</h1>
          <p className="text-gray-500 text-sm mt-0.5">Cadastro e gestão de estoque</p>
        </div>
        <button
          onClick={() => setForm({})}
          className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white
                     px-4 py-2 rounded-lg text-sm font-medium transition-colors"
        >
          <Plus size={16} /> Novo Produto
        </button>
      </div>

      {/* Busca */}
      <div className="relative mb-4">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="Buscar por nome..."
          value={busca}
          onChange={e => setBusca(e.target.value)}
          className="w-full pl-9 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm
                     focus:outline-none focus:ring-2 focus:ring-red-500"
        />
      </div>

      {/* Tabela */}
      <div className="bg-white rounded-xl shadow overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Carregando...</div>
        ) : isError ? (
          <div className="p-8 text-center">
            <p className="text-red-600 font-medium mb-1">Erro ao carregar produtos</p>
            <p className="text-gray-400 text-sm">{String((error as Error)?.message ?? 'Falha na requisição')}</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Código</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Nome</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Un.</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Preço Venda</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Estoque</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">PLU</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {produtos.map(p => (
                <tr key={p.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{p.codigoInterno}</td>
                  <td className="px-4 py-3 font-medium">
                    <div className="flex items-center gap-2">
                      {p.estoqueAtual <= p.estoqueMinimo && (
                        <AlertTriangle size={14} className="text-orange-500 shrink-0" />
                      )}
                      {p.nome}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{p.unidadeMedida}</td>
                  <td className="px-4 py-3 text-right font-medium tabular-nums">{brl(p.precoVenda)}</td>
                  <td className={`px-4 py-3 text-right tabular-nums font-medium
                    ${p.estoqueAtual <= p.estoqueMinimo ? 'text-orange-600' : 'text-gray-700'}`}>
                    {qtd3(p.estoqueAtual)} {p.unidadeMedida}
                  </td>
                  <td className="px-4 py-3 text-center text-gray-500 font-mono text-xs">
                    {p.codigoBalanca ? String(p.codigoBalanca).padStart(5, '0') : '—'}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium
                      ${p.ativo ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-100 text-gray-500'}`}>
                      {p.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1 justify-end">
                      <button
                        onClick={() => setForm(p)}
                        className="p-1.5 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-700"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => deletar.mutate(p.id)}
                        className="p-1.5 rounded hover:bg-red-50 text-gray-400 hover:text-red-500"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal de formulário */}
      {form !== null && (
        <ProdutoForm
          produto={form}
          onClose={() => setForm(null)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ['produtos'] })
            setForm(null)
          }}
        />
      )}
    </div>
  )
}
