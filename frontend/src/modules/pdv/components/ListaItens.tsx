import { Trash2 } from 'lucide-react'
import type { ItemVenda } from '@/types/venda'

interface Props {
  itens: ItemVenda[]
  onRemover: (id: number) => void
}

const brl = (v: number) =>
  v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const kg3 = (v: number) =>
  v.toLocaleString('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 })

export default function ListaItens({ itens, onRemover }: Props) {
  if (itens.length === 0) {
    return (
      <div className="flex-1 flex items-center justify-center text-gray-500 text-sm">
        Aguardando leitura do código de barras...
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-y-auto scrollbar-thin">
      <table className="w-full text-sm">
        <thead className="sticky top-0 bg-gray-800 text-gray-400 text-xs uppercase">
          <tr>
            <th className="px-4 py-2 text-left">#</th>
            <th className="px-4 py-2 text-left">Produto</th>
            <th className="px-4 py-2 text-right">Qtd</th>
            <th className="px-4 py-2 text-right">Preço/Kg</th>
            <th className="px-4 py-2 text-right">Total</th>
            <th className="px-2 py-2"></th>
          </tr>
        </thead>
        <tbody>
          {itens.map((item, idx) => (
            <tr
              key={item.id}
              className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors"
            >
              <td className="px-4 py-3 text-gray-500">{idx + 1}</td>
              <td className="px-4 py-3 font-medium">{item.produto.nome}</td>
              <td className="px-4 py-3 text-right tabular-nums text-gray-300">
                {item.produto.unidadeMedida === 'KG'
                  ? `${kg3(item.quantidade)} kg`
                  : `${item.quantidade} un`}
              </td>
              <td className="px-4 py-3 text-right tabular-nums text-gray-300">
                {brl(item.precoUnitario)}
              </td>
              <td className="px-4 py-3 text-right tabular-nums font-bold text-emerald-400">
                {brl(item.totalItem)}
              </td>
              <td className="px-2 py-3">
                <button
                  onClick={() => onRemover(item.id)}
                  className="p-1.5 rounded text-gray-500 hover:text-red-400 hover:bg-red-900/30 transition-colors"
                >
                  <Trash2 size={14} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
