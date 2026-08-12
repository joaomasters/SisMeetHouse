import { useState, useEffect } from 'react'
import { ShoppingBag, Wifi, WifiOff, Trash2 } from 'lucide-react'
import { useBarcodeScan } from '@/shared/hooks/useBarcodeScan'
import { usePdv } from './hooks/usePdv'
import ListaItens from './components/ListaItens'
import ModalPagamento from './components/ModalPagamento'

const brl = (v: number) =>
  v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export default function PDVPage() {
  const {
    venda, loading, scanLoading, totalVenda,
    adicionarItem, removerItem, fecharVenda, cancelarVenda,
  } = usePdv()

  const [showPagto, setShowPagto] = useState(false)
  const [hora, setHora]           = useState(new Date())

  // Relógio
  useEffect(() => {
    const t = setInterval(() => setHora(new Date()), 1000)
    return () => clearInterval(t)
  }, [])

  // Atalhos de teclado (F10 = Finalizar, Escape = Cancelar)
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'F10') { e.preventDefault(); setShowPagto(true) }
      if (e.key === 'Escape') { e.preventDefault(); /* abre confirmação */ }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  // Listener global do leitor de código de barras
  useBarcodeScan({
    onScan: adicionarItem,
    enabled: !showPagto,
  })

  return (
    <div className="h-screen flex flex-col bg-gray-950 text-white select-none overflow-hidden">

      {/* ── Topbar ── */}
      <header className="flex items-center justify-between px-6 py-3 bg-gray-900 border-b border-gray-800">
        <div className="flex items-center gap-3">
          <ShoppingBag size={20} className="text-red-400" />
          <span className="font-bold text-lg">PDV</span>
          {venda && (
            <span className="text-xs bg-gray-700 text-gray-300 px-2 py-0.5 rounded-full">
              Cupom #{venda.id}
            </span>
          )}
          {scanLoading && (
            <span className="text-xs bg-yellow-900 text-yellow-300 px-2 py-0.5 rounded-full animate-pulse">
              Processando...
            </span>
          )}
        </div>
        <div className="flex items-center gap-4 text-sm text-gray-400">
          {scanLoading
            ? <WifiOff size={16} className="text-yellow-500 animate-pulse" />
            : <Wifi size={16} className="text-emerald-500" />}
          <span className="tabular-nums">
            {hora.toLocaleTimeString('pt-BR')}
          </span>
          <span>{hora.toLocaleDateString('pt-BR')}</span>
        </div>
      </header>

      {/* ── Corpo ── */}
      <div className="flex flex-1 overflow-hidden">

        {/* Coluna esquerda: itens */}
        <div className="flex-1 flex flex-col overflow-hidden border-r border-gray-800">
          <ListaItens
            itens={venda?.itens ?? []}
            onRemover={removerItem}
          />
          {/* Rodapé da lista */}
          <div className="px-4 py-3 bg-gray-900 border-t border-gray-800 flex justify-between text-sm text-gray-400">
            <span>{venda?.itens?.length ?? 0} iten(s)</span>
            {venda?.desconto && venda.desconto > 0 && (
              <span>Desconto: -{brl(venda.desconto)}</span>
            )}
            <span>Subtotal: <span className="text-white font-medium tabular-nums">{brl(venda?.subtotal ?? 0)}</span></span>
          </div>
        </div>

        {/* Coluna direita: totais e ações */}
        <div className="w-72 flex flex-col gap-4 p-5 bg-gray-900">

          {/* Total */}
          <div className="bg-gray-800 rounded-2xl p-6 text-center shadow-inner">
            <p className="text-gray-400 text-xs uppercase tracking-widest mb-2">Total a Pagar</p>
            <p className="text-5xl font-black tabular-nums text-emerald-400 leading-none">
              {brl(totalVenda)}
            </p>
          </div>

          {/* Botões */}
          <button
            onClick={() => setShowPagto(true)}
            disabled={!venda?.itens?.length || loading}
            className="py-5 rounded-2xl text-xl font-bold
                       bg-emerald-600 hover:bg-emerald-500 active:scale-95
                       disabled:bg-gray-700 disabled:text-gray-500 disabled:cursor-not-allowed
                       transition-all duration-150"
          >
            FINALIZAR
            <span className="block text-xs font-normal opacity-70 mt-0.5">[F10]</span>
          </button>

          <button
            onClick={cancelarVenda}
            disabled={!venda}
            className="py-3 rounded-xl text-sm font-medium
                       bg-red-900/60 hover:bg-red-800 text-red-300
                       disabled:bg-gray-800 disabled:text-gray-600 disabled:cursor-not-allowed
                       flex items-center justify-center gap-2 transition-colors"
          >
            <Trash2 size={15} />
            CANCELAR VENDA
          </button>

          <div className="mt-auto text-center text-xs text-gray-600">
            Leitor de barcode ativo
          </div>
        </div>
      </div>

      {/* Modal de pagamento */}
      {showPagto && (
        <ModalPagamento
          totalVenda={totalVenda}
          vendaId={venda?.id}
          onConfirmar={async (pagamentos) => {
            await fecharVenda(pagamentos)
            setShowPagto(false)
          }}
          onCancelar={() => setShowPagto(false)}
        />
      )}
    </div>
  )
}
