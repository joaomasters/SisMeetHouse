import { useState, useCallback } from 'react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Venda, ItemVendaDTO, PagamentoDTO } from '@/types/venda'

const OPERADOR_ID = 1
const CAIXA_ID    = 1

export function usePdv() {
  const [venda, setVenda]         = useState<Venda | null>(null)
  const [loading, setLoading]     = useState(false)
  const [scanLoading, setScan]    = useState(false)

  const iniciarVenda = useCallback(async (clienteId?: number): Promise<Venda> => {
    const { data } = await api.post<Venda>('/pdv/vendas/abrir', {
      operadorId: OPERADOR_ID,
      caixaId:    CAIXA_ID,
      clienteId:  clienteId ?? null,
    })
    setVenda(data)
    return data
  }, [])

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
      toast.success(`${itemDto.nomeProduto} adicionado`, { duration: 1500 })
    } catch {
      // erro já tratado pelo interceptor do axios
    } finally {
      setScan(false)
    }
  }, [venda, iniciarVenda])

  const adicionarItemManual = useCallback(async (dto: ItemVendaDTO) => {
    if (!venda) {
      toast.error('Abra uma venda primeiro.')
      return
    }
    setLoading(true)
    try {
      const { data } = await api.post<Venda>(`/pdv/vendas/${venda.id}/itens`, dto)
      setVenda(data)
    } finally {
      setLoading(false)
    }
  }, [venda])

  const removerItem = useCallback(async (itemId: number) => {
    if (!venda) return
    const { data } = await api.delete<Venda>(`/pdv/vendas/${venda.id}/itens/${itemId}`)
    setVenda(data)
  }, [venda])

  const fecharVenda = useCallback(async (pagamentos: PagamentoDTO[]) => {
    if (!venda) return
    setLoading(true)
    try {
      await api.post(`/pdv/vendas/fechar`, { vendaId: venda.id, pagamentos })
      toast.success('Venda finalizada!')
      setVenda(null)
    } finally {
      setLoading(false)
    }
  }, [venda])

  const cancelarVenda = useCallback(async () => {
    if (!venda) return
    await api.post(`/pdv/vendas/${venda.id}/cancelar`)
    toast('Venda cancelada', { icon: '⚠️' })
    setVenda(null)
  }, [venda])

  return {
    venda,
    loading,
    scanLoading,
    totalVenda: venda?.total ?? 0,
    adicionarItem,
    adicionarItemManual,
    removerItem,
    fecharVenda,
    cancelarVenda,
    iniciarVenda,
  }
}
