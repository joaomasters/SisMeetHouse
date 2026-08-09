import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Scissors, ChevronRight } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { FichaDesossa, ExecutarDesossaDTO } from '@/types/produto'

const kg3 = (v: number) => v.toLocaleString('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 })

export default function DesossaPage() {
  const qc = useQueryClient()
  const [fichaSel, setFichaSel]         = useState<FichaDesossa | null>(null)
  const [qtdEntrada, setQtdEntrada]     = useState('')
  const [custoPorKg, setCustoPorKg]     = useState('')
  const [qtdsReais, setQtdsReais]       = useState<Record<number, string>>({})

  const { data: fichas = [] } = useQuery<FichaDesossa[]>({
    queryKey: ['fichas-desossa'],
    queryFn: () => api.get('/estoque/fichas-desossa').then(r => r.data),
  })

  const executar = useMutation({
    mutationFn: (dto: ExecutarDesossaDTO) =>
      api.post('/estoque/desossa/executar', dto),
    onSuccess: () => {
      toast.success('Desossa executada e estoque atualizado!')
      qc.invalidateQueries({ queryKey: ['produtos'] })
      setFichaSel(null)
      setQtdEntrada('')
      setCustoPorKg('')
      setQtdsReais({})
    },
  })

  function handleExecutar() {
    if (!fichaSel || !qtdEntrada) return
    const dto: ExecutarDesossaDTO = {
      fichaDesossaId: fichaSel.id,
      quantidadeKgEntrada: parseFloat(qtdEntrada),
      custoPorKg: custoPorKg ? parseFloat(custoPorKg) : undefined,
      quantidadesReais: Object.keys(qtdsReais).reduce((acc, k) => {
        const v = parseFloat(qtdsReais[Number(k)])
        if (!isNaN(v)) acc[Number(k)] = v
        return acc
      }, {} as Record<number, number>),
      usuarioId: 1,
    }
    executar.mutate(dto)
  }

  const qtdNum = parseFloat(qtdEntrada) || 0

  return (
    <div className="p-6 max-w-4xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Scissors size={24} className="text-red-600" /> Desossa / Rendimento
        </h1>
        <p className="text-gray-500 text-sm mt-0.5">
          Processa peças inteiras em cortes filhos com rateio automático de custo
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* Seleção de ficha */}
        <div>
          <h2 className="text-sm font-semibold text-gray-700 mb-3">Ficha de Desossa</h2>
          <div className="space-y-2">
            {fichas.map(f => (
              <button
                key={f.id}
                onClick={() => setFichaSel(f)}
                className={`w-full flex items-center justify-between p-4 rounded-xl border-2 text-left transition-all
                  ${fichaSel?.id === f.id
                    ? 'border-red-500 bg-red-50'
                    : 'border-gray-200 bg-white hover:border-gray-300'}`}
              >
                <div>
                  <p className="font-medium text-gray-900">{f.nome}</p>
                  <p className="text-xs text-gray-500 mt-0.5">
                    Produto pai: {f.produtoPai.nome} • {f.itens.length} cortes
                  </p>
                </div>
                <ChevronRight size={16} className="text-gray-400 shrink-0" />
              </button>
            ))}
            {fichas.length === 0 && (
              <p className="text-gray-400 text-sm">Nenhuma ficha cadastrada.</p>
            )}
          </div>
        </div>

        {/* Formulário de execução */}
        {fichaSel && (
          <div className="bg-white rounded-xl shadow p-5 space-y-4">
            <h2 className="font-semibold text-gray-900">
              Executar: <span className="text-red-600">{fichaSel.nome}</span>
            </h2>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Quantidade (kg) *</label>
                <input
                  type="number" step="0.001" min="0.001"
                  value={qtdEntrada}
                  onChange={e => setQtdEntrada(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                             focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="100.000"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Custo / kg (R$)</label>
                <input
                  type="number" step="0.01"
                  value={custoPorKg}
                  onChange={e => setCustoPorKg(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                             focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="25.00"
                />
              </div>
            </div>

            {/* Itens da ficha com quantidades reais */}
            <div>
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Quantidades Reais (opcional)
              </p>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {fichaSel.itens.map(item => {
                  const prevista = qtdNum * (item.percentualRendimento / 100)
                  return (
                    <div key={item.id} className="flex items-center gap-3 bg-gray-50 rounded-lg px-3 py-2">
                      <div className="flex-1">
                        <p className="text-sm font-medium">{item.produtoFilho.nome}</p>
                        <p className="text-xs text-gray-400">
                          {item.percentualRendimento}% — Previsto: {kg3(prevista)} kg
                        </p>
                      </div>
                      <input
                        type="number" step="0.001"
                        value={qtdsReais[item.produtoFilho.id] ?? ''}
                        onChange={e => setQtdsReais(prev => ({
                          ...prev,
                          [item.produtoFilho.id]: e.target.value,
                        }))}
                        placeholder={kg3(prevista)}
                        className="w-28 border border-gray-300 rounded px-2 py-1 text-sm text-right"
                      />
                      <span className="text-xs text-gray-500">kg</span>
                    </div>
                  )
                })}
              </div>
            </div>

            <button
              onClick={handleExecutar}
              disabled={!qtdEntrada || executar.isPending}
              className="w-full py-3 bg-red-600 hover:bg-red-700 text-white rounded-xl font-medium
                         disabled:opacity-60 transition-colors"
            >
              {executar.isPending ? 'Processando...' : 'Executar Desossa'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
