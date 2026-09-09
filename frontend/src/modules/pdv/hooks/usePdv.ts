import { useState, useCallback, useEffect } from 'react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Venda, ItemVendaDTO, PagamentoDTO } from '@/types/venda'

const OPERADOR_ID = 1

export function usePdv() {
  const [venda, setVenda]         = useState<Venda | null>(null)
  const [caixaId, setCaixaId]     = useState<number | null>(null)
  const [comandas, setComandas]   = useState<Venda[]>([])
  const [loading, setLoading]     = useState(false)
  const [scanLoading, setScan]    = useState(false)

  // Descobre o caixa realmente aberto agora, em vez de fixar um id que pode
  // estar fechado (isso travava toda venda com "Caixa está fechado").
  useEffect(() => {
    api.get<{ id: number }>('/pdv/caixa/aberto')
      .then(({ data }) => setCaixaId(data.id))
      .catch(() => toast.error('Nenhum caixa aberto. Abra um caixa antes de vender.'))
  }, [])

  const carregarComandas = useCallback(async () => {
    if (!caixaId) return
    const { data } = await api.get<Venda[]>('/pdv/vendas/abertas', { params: { caixaId } })
    setComandas(data)
  }, [caixaId])

  // Sempre que o caixa for identificado, já carrega as comandas em aberto
  useEffect(() => { carregarComandas() }, [carregarComandas])

  const selecionarComanda = useCallback(async (vendaId: number) => {
    const { data } = await api.get<Venda>(`/pdv/vendas/${vendaId}`)
    setVenda(data)
  }, [])

  // Atualiza uma comanda específica na lista local, sem precisar recarregar tudo da API
  const atualizarComandaLocal = useCallback((vendaAtualizada: Venda) => {
    setComandas(prev => prev.map(c => c.id === vendaAtualizada.id ? vendaAtualizada : c))
  }, [])

  const iniciarVenda = useCallback(async (clienteId?: number): Promise<Venda> => {
    if (!caixaId) throw new Error('Nenhum caixa aberto.')
    const { data } = await api.post<Venda>('/pdv/vendas/abrir', {
      operadorId: OPERADOR_ID,
      caixaId,
      clienteId:  clienteId ?? null,
    })
    setVenda(data)
    carregarComandas()
    return data
  }, [caixaId, carregarComandas])

  const adicionarItem = useCallback(async (barcode: string) => {
    setScan(true)
    try {
      // 1. Decodificar barcode
      const { data: itemDto } = await api.get<ItemVendaDTO>(`/pdv/barcode/${barcode}`)

      // 2. Garantir que há uma venda aberta
      let vendaAtual = venda
      if (!vendaAtual) {
        vendaAtual = await iniciarVenda()
      }

      // 3. Adicionar item
      const { data: vendaAtualizada } = await api.post<Venda>(
        `/pdv/vendas/${vendaAtual.id}/itens`,
        itemDto
      )
      setVenda(vendaAtualizada)
      atualizarComandaLocal(vendaAtualizada)
      toast.success(`${itemDto.nomeProduto} adicionado`, { duration: 1500 })
    } catch {
      // erro já tratado pelo interceptor do axios
    } finally {
      setScan(false)
    }
  }, [venda, iniciarVenda, atualizarComandaLocal])

  const adicionarItemManual = useCallback(async (dto: ItemVendaDTO) => {
    if (!venda) {
      toast.error('Abra uma venda primeiro.')
      return
    }
    setLoading(true)
    try {
      const { data } = await api.post<Venda>(`/pdv/vendas/${venda.id}/itens`, dto)
      setVenda(data)
      atualizarComandaLocal(data)
    } finally {
      setLoading(false)
    }
  }, [venda, atualizarComandaLocal])

  const removerItem = useCallback(async (itemId: number) => {
    if (!venda) return
    const { data } = await api.delete<Venda>(`/pdv/vendas/${venda.id}/itens/${itemId}`)
    setVenda(data)
    atualizarComandaLocal(data)
  }, [venda, atualizarComandaLocal])

  const fecharVenda = useCallback(async (pagamentos: PagamentoDTO[]) => {
    if (!venda) return
    setLoading(true)
    try {
      await api.post(`/pdv/vendas/fechar`, { vendaId: venda.id, pagamentos })
      toast.success('Venda finalizada!')
      setVenda(null)
      carregarComandas()
    } finally {
      setLoading(false)
    }
  }, [venda, carregarComandas])

  const cancelarVenda = useCallback(async () => {
    if (!venda) return
    await api.post(`/pdv/vendas/${venda.id}/cancelar`)
    toast('Venda cancelada', { icon: '⚠️' })
    setVenda(null)
    carregarComandas()
  }, [venda, carregarComandas])

  return {
    venda,
    comandas,
    caixaId,
    loading,
    scanLoading,
    totalVenda: venda?.total ?? 0,
    adicionarItem,
    adicionarItemManual,
    removerItem,
    fecharVenda,
    cancelarVenda,
    iniciarVenda,
    selecionarComanda,
    novaComanda: () => setVenda(null), // limpa a comanda ativa, sem fechar nenhuma
  }
}