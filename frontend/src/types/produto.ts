export interface Categoria {
  id: number
  nome: string
  descricao?: string
}

export interface Produto {
  id: number
  codigoInterno: string
  codigoBalanca?: number
  ean13?: string
  nome: string
  descricao?: string
  unidadeMedida: 'KG' | 'UN' | 'CX' | 'G'
  tipoProduto: 'CORTE' | 'INDUSTRIALIZADO' | 'INSUMO' | 'SUBPRODUTO'
  precoCusto?: number
  precoVenda: number
  estoqueAtual: number
  estoqueMinimo: number
  categoria?: Categoria
  temFichaDesossa: boolean
  ativo: boolean
}

export interface FichaDesossaItem {
  id: number
  produtoFilho: Produto
  percentualRendimento: number
  sequencia: number
}

export interface FichaDesossa {
  id: number
  produtoPai: Produto
  nome: string
  descricao?: string
  perdaTotalPercentual: number
  ativo: boolean
  itens: FichaDesossaItem[]
}

export interface ExecutarDesossaDTO {
  fichaDesossaId: number
  quantidadeKgEntrada: number
  custoPorKg?: number
  quantidadesReais?: Record<number, number>
  usuarioId?: number
  observacao?: string
  recebimentoId?: number | null
}
