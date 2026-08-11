import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Truck, Plus, FileText, X, ChevronDown, ChevronUp, Upload } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'

interface Produto { id: number; nome: string; unidadeMedida: string }
interface RecItem { produtoId: number; quantidade: string; custoUnitario: string }

interface Recebimento {
  id: number
  fornecedor: string
  numeroNf: string
  serieNf: string
  chaveNf: string
  dataEmissao: string
  dataRecebimento: string
  valorTotal: number
  status: string
  xmlNf: string | null
  observacao: string | null
  itens: { id: number; produto: { nome: string; unidadeMedida: string }; quantidade: number; custoUnitario: number; custoTotal: number }[]
}

const brl = (v?: number) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const parseNum = (s: string) => parseFloat(s.replace(',', '.')) || 0

export default function RecebimentoPage() {
  const qc = useQueryClient()
  const fileRef = useRef<HTMLInputElement>(null)
  const xmlRef  = useRef<HTMLInputElement>(null)

  const [showForm, setShowForm]       = useState(false)
  const [expandId, setExpandId]       = useState<number | null>(null)
  const [xmlViewId, setXmlViewId]     = useState<number | null>(null)
  const [uploadId, setUploadId]       = useState<number | null>(null)

  // Form state
  const [fornecedor, setFornecedor]   = useState('')
  const [numeroNf, setNumeroNf]       = useState('')
  const [serieNf, setSerieNf]         = useState('1')
  const [chaveNf, setChaveNf]         = useState('')
  const [dataEmissao, setDataEmissao] = useState('')
  const [valorTotal, setValorTotal]   = useState('')
  const [observacao, setObservacao]   = useState('')
  const [xmlNf, setXmlNf]             = useState('')
  const [itens, setItens]             = useState<RecItem[]>([{ produtoId: 0, quantidade: '', custoUnitario: '' }])

  const { data: produtos = [] } = useQuery<Produto[]>({
    queryKey: ['produtos'],
    queryFn: () => api.get('/estoque/produtos').then(r => r.data),
  })

  const { data: recebimentos = [], isLoading } = useQuery<Recebimento[]>({
    queryKey: ['recebimentos'],
    queryFn: () => api.get('/estoque/recebimentos').then(r => r.data),
  })

  const registrar = useMutation({
    mutationFn: () => api.post('/estoque/recebimentos', {
      fornecedor,
      numeroNf: numeroNf || null,
      serieNf,
      chaveNf: chaveNf || null,
      dataEmissao: dataEmissao || null,
      valorTotal: valorTotal ? parseNum(valorTotal) : null,
      observacao: observacao || null,
      xmlNf: xmlNf || null,
      itens: itens.filter(i => i.produtoId > 0 && i.quantidade).map(i => ({
        produtoId: i.produtoId,
        quantidade: parseNum(i.quantidade),
        custoUnitario: parseNum(i.custoUnitario) || null,
      })),
    }),
    onSuccess: () => {
      toast.success('Recebimento registrado e estoque atualizado!')
      qc.invalidateQueries({ queryKey: ['recebimentos'] })
      qc.invalidateQueries({ queryKey: ['produtos'] })
      resetForm()
    },
  })

  const uploadXml = useMutation({
    mutationFn: ({ id, xml }: { id: number; xml: string }) =>
      api.put(`/estoque/recebimentos/${id}/xml`, { xml }),
    onSuccess: () => {
      toast.success('XML vinculado!')
      qc.invalidateQueries({ queryKey: ['recebimentos'] })
      setUploadId(null)
    },
  })

  function resetForm() {
    setShowForm(false)
    setFornecedor(''); setNumeroNf(''); setSerieNf('1')
    setChaveNf(''); setDataEmissao(''); setValorTotal('')
    setObservacao(''); setXmlNf('')
    setItens([{ produtoId: 0, quantidade: '', custoUnitario: '' }])
  }

  function addItem() {
    setItens(prev => [...prev, { produtoId: 0, quantidade: '', custoUnitario: '' }])
  }

  function removeItem(idx: number) {
    setItens(prev => prev.filter((_, i) => i !== idx))
  }

  function updateItem(idx: number, field: keyof RecItem, val: string | number) {
    setItens(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))
  }

  function handleXmlFile(e: React.ChangeEvent<HTMLInputElement>, forId?: number) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = ev => {
      const content = ev.target?.result as string
      if (forId) {
        uploadXml.mutate({ id: forId, xml: content })
      } else {
        setXmlNf(content)
        toast.success('XML carregado no formulário')
      }
    }
    reader.readAsText(file)
    e.target.value = ''
  }

  const canSave = fornecedor.trim() !== '' && itens.some(i => i.produtoId > 0 && parseNum(i.quantidade) > 0)

  const rec = recebimentos.find(r => r.id === xmlViewId)

  return (
    <div className="p-6 max-w-5xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Truck size={24} className="text-red-600" /> Recebimento de Mercadoria
          </h1>
          <p className="text-gray-500 text-sm mt-0.5">Entrada de NF • atualiza estoque automaticamente</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700"
        >
          <Plus size={16} /> Nova Entrada
        </button>
      </div>

      {/* Lista */}
      <div className="space-y-2">
        {isLoading && <p className="text-gray-400 text-sm">Carregando...</p>}
        {recebimentos.map(r => (
          <div key={r.id} className="bg-white rounded-xl border overflow-hidden">
            <div
              className="flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-gray-50"
              onClick={() => setExpandId(expandId === r.id ? null : r.id)}
            >
              <div className="flex-1 grid grid-cols-4 gap-4">
                <div>
                  <p className="text-xs text-gray-400 font-medium">Fornecedor</p>
                  <p className="font-semibold text-gray-800 text-sm">{r.fornecedor}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-medium">NF</p>
                  <p className="text-sm text-gray-700">{r.numeroNf ? `${r.numeroNf}-${r.serieNf}` : '—'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-medium">Recebimento</p>
                  <p className="text-sm text-gray-700">
                    {r.dataRecebimento ? new Date(r.dataRecebimento).toLocaleDateString('pt-BR') : '—'}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-medium">Valor Total</p>
                  <p className="text-sm font-medium text-gray-800">{brl(r.valorTotal)}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {r.xmlNf ? (
                  <button
                    onClick={e => { e.stopPropagation(); setXmlViewId(r.id) }}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100"
                  >
                    <FileText size={12} /> XML
                  </button>
                ) : (
                  <label className="flex items-center gap-1 px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 cursor-pointer">
                    <Upload size={12} /> XML
                    <input
                      type="file" accept=".xml" className="hidden"
                      onChange={e => handleXmlFile(e, r.id)}
                    />
                  </label>
                )}
                {expandId === r.id ? <ChevronUp size={16} className="text-gray-400" /> : <ChevronDown size={16} className="text-gray-400" />}
              </div>
            </div>

            {expandId === r.id && (
              <div className="border-t bg-gray-50 px-5 py-4">
                {r.chaveNf && (
                  <p className="text-xs text-gray-500 mb-3 font-mono break-all">Chave: {r.chaveNf}</p>
                )}
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs text-gray-500 uppercase">
                      <th className="text-left pb-2">Produto</th>
                      <th className="text-right pb-2">Quantidade</th>
                      <th className="text-right pb-2">Custo Unit.</th>
                      <th className="text-right pb-2">Custo Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {r.itens.map(it => (
                      <tr key={it.id} className="border-t border-gray-200">
                        <td className="py-2 font-medium">{it.produto.nome}</td>
                        <td className="py-2 text-right">{it.quantidade} {it.produto.unidadeMedida}</td>
                        <td className="py-2 text-right">{brl(it.custoUnitario)}</td>
                        <td className="py-2 text-right font-medium">{brl(it.custoTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {r.observacao && (
                  <p className="text-xs text-gray-500 mt-3">Obs: {r.observacao}</p>
                )}
              </div>
            )}
          </div>
        ))}
        {!isLoading && recebimentos.length === 0 && (
          <div className="bg-white rounded-xl border p-10 text-center text-gray-400">
            Nenhum recebimento registrado
          </div>
        )}
      </div>

      {/* Modal XML Viewer */}
      {xmlViewId && rec && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl w-full max-w-4xl h-[80vh] flex flex-col">
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-700">
              <div>
                <p className="font-semibold text-white">XML da NF</p>
                <p className="text-xs text-gray-400">{rec.fornecedor} — NF {rec.numeroNf || 'S/N'}</p>
              </div>
              <div className="flex items-center gap-3">
                <label className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg cursor-pointer hover:bg-blue-700">
                  <Upload size={12} /> Atualizar XML
                  <input type="file" accept=".xml" className="hidden" onChange={e => handleXmlFile(e, xmlViewId)} />
                </label>
                <button onClick={() => setXmlViewId(null)} className="text-gray-400 hover:text-white">
                  <X size={20} />
                </button>
              </div>
            </div>
            <pre className="flex-1 overflow-auto p-5 text-xs text-green-400 font-mono whitespace-pre-wrap">
              {rec.xmlNf}
            </pre>
          </div>
        </div>
      )}

      {/* Modal Nova Entrada */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-start justify-center z-50 p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl w-full max-w-2xl my-4 p-6 space-y-5">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold">Nova Entrada de Mercadoria</h2>
              <button onClick={resetForm} className="text-gray-400 hover:text-gray-700"><X size={20} /></button>
            </div>

            {/* Dados da NF */}
            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">Fornecedor *</label>
                <input value={fornecedor} onChange={e => setFornecedor(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="Nome do fornecedor" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Número NF</label>
                <input value={numeroNf} onChange={e => setNumeroNf(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm"
                  placeholder="000001" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Série</label>
                <input value={serieNf} onChange={e => setSerieNf(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm"
                  placeholder="1" />
              </div>
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">Chave de Acesso (44 dígitos)</label>
                <input value={chaveNf} onChange={e => setChaveNf(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm font-mono"
                  placeholder="00000000000000000000000000000000000000000000" maxLength={44} />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Data de Emissão</label>
                <input type="date" value={dataEmissao} onChange={e => setDataEmissao(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Valor Total NF (R$)</label>
                <input value={valorTotal} onChange={e => setValorTotal(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm"
                  placeholder="0,00" />
              </div>
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">XML da NF</label>
                <div className="mt-1 flex items-center gap-2">
                  <label className="flex items-center gap-2 px-3 py-2 border border-dashed rounded-lg text-sm text-gray-500 cursor-pointer hover:bg-gray-50 flex-1">
                    <Upload size={14} />
                    {xmlNf ? '✓ XML carregado' : 'Selecionar arquivo .xml'}
                    <input ref={fileRef} type="file" accept=".xml" className="hidden" onChange={e => handleXmlFile(e)} />
                  </label>
                  {xmlNf && (
                    <button onClick={() => setXmlNf('')} className="text-red-400 hover:text-red-600 text-xs">Remover</button>
                  )}
                </div>
              </div>
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">Observação</label>
                <input value={observacao} onChange={e => setObservacao(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
            </div>

            {/* Itens */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Produtos Recebidos *</label>
                <button onClick={addItem} className="flex items-center gap-1 text-xs text-red-600 hover:text-red-700">
                  <Plus size={12} /> Adicionar item
                </button>
              </div>
              <div className="space-y-2">
                {itens.map((item, idx) => (
                  <div key={idx} className="grid grid-cols-12 gap-2 items-end bg-gray-50 rounded-lg p-3">
                    <div className="col-span-5">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Produto</label>}
                      <select
                        value={item.produtoId}
                        onChange={e => updateItem(idx, 'produtoId', parseInt(e.target.value))}
                        className="w-full border rounded px-2 py-1.5 text-sm bg-white"
                      >
                        <option value={0}>Selecione...</option>
                        {produtos.map(p => (
                          <option key={p.id} value={p.id}>{p.nome}</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-span-3">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Quantidade</label>}
                      <input
                        value={item.quantidade}
                        onChange={e => updateItem(idx, 'quantidade', e.target.value)}
                        placeholder="0,000"
                        className="w-full border rounded px-2 py-1.5 text-sm"
                      />
                    </div>
                    <div className="col-span-3">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Custo/un (R$)</label>}
                      <input
                        value={item.custoUnitario}
                        onChange={e => updateItem(idx, 'custoUnitario', e.target.value)}
                        placeholder="0,00"
                        className="w-full border rounded px-2 py-1.5 text-sm"
                      />
                    </div>
                    <div className="col-span-1 flex justify-center">
                      {itens.length > 1 && (
                        <button onClick={() => removeItem(idx)} className="text-red-400 hover:text-red-600 mt-1">
                          <X size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button onClick={resetForm} className="flex-1 py-2.5 border rounded-lg text-sm text-gray-600 hover:bg-gray-50">
                Cancelar
              </button>
              <button
                onClick={() => registrar.mutate()}
                disabled={!canSave || registrar.isPending}
                className="flex-1 py-2.5 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50"
              >
                {registrar.isPending ? 'Registrando...' : 'Registrar Entrada'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
