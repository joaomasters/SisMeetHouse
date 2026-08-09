import { useForm } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'
import { X } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Produto } from '@/types/produto'

interface Props {
  produto: Partial<Produto>
  onClose: () => void
  onSaved: () => void
}

export default function ProdutoForm({ produto, onClose, onSaved }: Props) {
  const isEdicao = Boolean(produto.id)
  const { register, handleSubmit, formState: { errors } } = useForm<Produto>({
    defaultValues: produto,
  })

  const salvar = useMutation({
    mutationFn: (data: Produto) =>
      isEdicao
        ? api.put(`/estoque/produtos/${produto.id}`, data)
        : api.post('/estoque/produtos', data),
    onSuccess: () => {
      toast.success(isEdicao ? 'Produto atualizado!' : 'Produto criado!')
      onSaved()
    },
  })

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl">

        <div className="flex items-center justify-between px-6 py-4 border-b">
          <h2 className="font-bold text-gray-900">{isEdicao ? 'Editar Produto' : 'Novo Produto'}</h2>
          <button onClick={onClose}><X size={20} className="text-gray-400" /></button>
        </div>

        <form onSubmit={handleSubmit(d => salvar.mutate(d))} className="px-6 py-5 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Nome *</label>
              <input
                {...register('nome', { required: 'Obrigatório' })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
              {errors.nome && <p className="text-red-500 text-xs mt-1">{errors.nome.message}</p>}
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Unidade *</label>
              <select
                {...register('unidadeMedida', { required: true })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              >
                <option value="KG">KG — Quilo</option>
                <option value="UN">UN — Unidade</option>
                <option value="CX">CX — Caixa</option>
                <option value="G">G — Grama</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Tipo *</label>
              <select
                {...register('tipoProduto', { required: true })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              >
                <option value="CORTE">Corte de Carne</option>
                <option value="INDUSTRIALIZADO">Industrializado</option>
                <option value="INSUMO">Insumo</option>
                <option value="SUBPRODUTO">Subproduto (sebo, osso...)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">PLU Balança</label>
              <input
                type="number"
                {...register('codigoBalanca')}
                placeholder="00001"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Preço Venda (R$) *</label>
              <input
                type="number" step="0.01"
                {...register('precoVenda', { required: true, min: 0.01 })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Custo (R$)</label>
              <input
                type="number" step="0.0001"
                {...register('precoCusto')}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Estoque Mín.</label>
              <input
                type="number" step="0.001"
                {...register('estoqueMinimo')}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">EAN-13 (industrializado)</label>
            <input
              {...register('ean13')}
              maxLength={13}
              placeholder="0000000000000"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-red-500"
            />
          </div>

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={salvar.isPending}
              className="flex-1 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium
                         disabled:opacity-60 transition-colors"
            >
              {salvar.isPending ? 'Salvando...' : 'Salvar Produto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
