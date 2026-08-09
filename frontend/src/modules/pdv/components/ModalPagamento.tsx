import { useState } from 'react'
import { X, Plus, Trash2 } from 'lucide-react'
import type { PagamentoDTO } from '@/types/venda'

interface Props {
  totalVenda: number
  onConfirmar: (pagamentos: PagamentoDTO[]) => void
  onCancelar: () => void
}

const FORMAS = [
  { value: 'DINHEIRO', label: 'Dinheiro',       cor: 'bg-green-600' },
  { value: 'DEBITO',   label: 'Débito',          cor: 'bg-blue-600' },
  { value: 'CREDITO',  label: 'Crédito',         cor: 'bg-purple-600' },
  { value: 'PIX',      label: 'Pix',             cor: 'bg-teal-600' },
  { value: 'FIADO',    label: 'Fiado/Caderneta', cor: 'bg-orange-600' },
]

const brl = (v: number) =>
  v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export default function ModalPagamento({ totalVenda, onConfirmar, onCancelar }: Props) {
  const [pagamentos, setPagamentos] = useState<PagamentoDTO[]>([])
  const [forma, setForma]           = useState('DINHEIRO')
  const [valor, setValor]           = useState('')

  const totalPago = pagamentos.reduce((s, p) => s + p.valor, 0)
  const saldo     = totalVenda - totalPago
  const troco     = totalPago > totalVenda ? totalPago - totalVenda : 0

  function adicionarPagamento() {
    const v = parseFloat(valor.replace(',', '.'))
    if (!v || v <= 0) return
    setPagamentos(prev => [...prev, { formaPagamento: forma, valor: v }])
    setValor('')
  }

  function remover(i: number) {
    setPagamentos(prev => prev.filter((_, idx) => idx !== i))
  }

  function confirmar() {
    if (totalPago < totalVenda - 0.01) return
    onConfirmar(pagamentos)
  }

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-900 rounded-2xl w-full max-w-md shadow-2xl text-white">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-700">
          <h2 className="text-lg font-bold">Pagamento</h2>
          <button onClick={onCancelar} className="p-1 hover:text-red-400"><X size={20} /></button>
        </div>

        <div className="px-6 py-4 space-y-4">
          {/* Totais */}
          <div className="grid grid-cols-3 gap-2 text-center">
            <div className="bg-gray-800 rounded-lg p-3">
              <p className="text-xs text-gray-400">Total</p>
              <p className="text-lg font-bold tabular-nums text-white">{brl(totalVenda)}</p>
            </div>
            <div className="bg-gray-800 rounded-lg p-3">
              <p className="text-xs text-gray-400">Pago</p>
              <p className="text-lg font-bold tabular-nums text-emerald-400">{brl(totalPago)}</p>
            </div>
            <div className={`rounded-lg p-3 ${saldo > 0 ? 'bg-red-900/50' : 'bg-emerald-900/50'}`}>
              <p className="text-xs text-gray-400">{troco > 0 ? 'Troco' : 'Restante'}</p>
              <p className={`text-lg font-bold tabular-nums ${troco > 0 ? 'text-yellow-400' : saldo > 0 ? 'text-red-400' : 'text-emerald-400'}`}>
                {brl(troco > 0 ? troco : saldo)}
              </p>
            </div>
          </div>

          {/* Selector de forma */}
          <div className="flex gap-2 flex-wrap">
            {FORMAS.map(f => (
              <button
                key={f.value}
                onClick={() => setForma(f.value)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all
                  ${forma === f.value ? f.cor + ' text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'}`}
              >
                {f.label}
              </button>
            ))}
          </div>

          {/* Input valor */}
          <div className="flex gap-2">
            <input
              type="number"
              placeholder={`Valor (ex: ${brl(Math.max(0, saldo))})`}
              value={valor}
              onChange={e => setValor(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && adicionarPagamento()}
              className="flex-1 bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:border-emerald-500"
            />
            <button
              onClick={adicionarPagamento}
              className="bg-emerald-600 hover:bg-emerald-500 px-4 rounded-lg transition-colors"
            >
              <Plus size={18} />
            </button>
          </div>

          {/* Lista de pagamentos */}
          {pagamentos.length > 0 && (
            <div className="space-y-1 max-h-32 overflow-y-auto">
              {pagamentos.map((p, i) => (
                <div key={i} className="flex items-center justify-between bg-gray-800 rounded px-3 py-2 text-sm">
                  <span className="text-gray-300">{p.formaPagamento}</span>
                  <div className="flex items-center gap-2">
                    <span className="tabular-nums font-medium">{brl(p.valor)}</span>
                    <button onClick={() => remover(i)} className="text-gray-500 hover:text-red-400">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-700 flex gap-3">
          <button
            onClick={onCancelar}
            className="flex-1 py-3 rounded-xl bg-gray-700 hover:bg-gray-600 text-sm transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={confirmar}
            disabled={totalPago < totalVenda - 0.01}
            className="flex-1 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500
                       disabled:bg-gray-600 disabled:cursor-not-allowed
                       font-bold text-sm transition-colors"
          >
            Confirmar Pagamento
          </button>
        </div>
      </div>
    </div>
  )
}
