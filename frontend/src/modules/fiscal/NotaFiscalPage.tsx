import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileText, Plus, X, ChevronDown, ChevronUp, Upload, CheckCircle, XCircle } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'

interface Produto  { id: number; nome: string; precoVenda: number }
interface Cliente  { id: number; nome: string }
interface NfItem   { produtoId: number | null; descricao: string; quantidade: string; valorUnitario: string }

interface NotaFiscal {
  id: number
  numeroNf: string | null
  serieNf: string
  cliente: { id: number; nome: string } | null
  naturezaOperacao: string
  dataEmissao: string
  valorProdutos: number
  valorDesconto: number
  valorTotal: number
  status: string
  xmlNf: string | null
  itens: { id: number; produto: { nome: string } | null; descricao: string; quantidade: number; valorUnitario: number; valorTotal: number }[]
}

const brl = (v?: number) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const parseNum = (s: string) => parseFloat(s.replace(',', '.')) || 0

const STATUS_COR: Record<string, string> = {
  PENDENTE:  'bg-yellow-100 text-yellow-700',
  EMITIDA:   'bg-emerald-100 text-emerald-700',
  CANCELADA: 'bg-red-100 text-red-600',
}

export default function NotaFiscalPage() {
  const qc = useQueryClient()

  const [showForm, setShowForm]       = useState(false)
  const [expandId, setExpandId]       = useState<number | null>(null)
  const [xmlViewId, setXmlViewId]     = useState<number | null>(null)

  // Form
  const [clienteId, setClienteId]     = useState('')
  const [numeroNf, setNumeroNf]       = useState('')
  const [serieNf, setSerieNf]         = useState('1')
  const [natureza, setNatureza]       = useState('VENDA DE MERCADORIAS')
  const [observacao, setObservacao]   = useState('')
  const [itens, setItens]             = useState<NfItem[]>([{ produtoId: null, descricao: '', quantidade: '', valorUnitario: '' }])

  const { data: produtos = [] } = useQuery<Produto[]>({
    queryKey: ['produtos'],
    queryFn: () => api.get('/estoque/produtos').then(r => r.data),
  })

  const { data: clientes = [] } = useQuery<Cliente[]>({
    queryKey: ['clientes'],
    queryFn: () => api.get('/financeiro/clientes').then(r => r.data),
  })

  const { data: notas = [], isLoading } = useQuery<NotaFiscal[]>({
    queryKey: ['notas-fiscais'],
    queryFn: () => api.get('/fiscal/notas').then(r => r.data),
  })

  const criar = useMutation({
    mutationFn: () => api.post('/fiscal/notas', {
      clienteId: clienteId ? parseInt(clienteId) : null,
      numeroNf: numeroNf || null,
      serieNf,
      naturezaOperacao: natureza,
      observacao: observacao || null,
      itens: itens.filter(i => i.quantidade && i.valorUnitario).map(i => ({
        produtoId: i.produtoId,
        descricao: i.descricao || null,
        quantidade: parseNum(i.quantidade),
        valorUnitario: parseNum(i.valorUnitario),
      })),
    }),
    onSuccess: () => {
      toast.success('NF criada!')
      qc.invalidateQueries({ queryKey: ['notas-fiscais'] })
      resetForm()
    },
  })

  const atualizarStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      api.put(`/fiscal/notas/${id}/status`, { status }),
    onSuccess: () => {
      toast.success('Status atualizado!')
      qc.invalidateQueries({ queryKey: ['notas-fiscais'] })
    },
  })

  const uploadXml = useMutation({
    mutationFn: ({ id, xml }: { id: number; xml: string }) =>
      api.put(`/fiscal/notas/${id}/xml`, { xml }),
    onSuccess: () => {
      toast.success('XML vinculado e status atualizado para EMITIDA!')
      qc.invalidateQueries({ queryKey: ['notas-fiscais'] })
    },
  })

  function resetForm() {
    setShowForm(false)
    setClienteId(''); setNumeroNf(''); setSerieNf('1')
    setNatureza('VENDA DE MERCADORIAS'); setObservacao('')
    setItens([{ produtoId: null, descricao: '', quantidade: '', valorUnitario: '' }])
  }

  function addItem() {
    setItens(prev => [...prev, { produtoId: null, descricao: '', quantidade: '', valorUnitario: '' }])
  }

  function removeItem(idx: number) {
    setItens(prev => prev.filter((_, i) => i !== idx))
  }

  function updateItem(idx: number, field: keyof NfItem, val: string | number | null) {
    setItens(prev => prev.map((item, i) => {
      if (i !== idx) return item
      if (field === 'produtoId' && typeof val === 'number') {
        const p = produtos.find(p => p.id === val)
        return { ...item, produtoId: val, descricao: p?.nome ?? '', valorUnitario: p ? String(p.precoVenda) : '' }
      }
      return { ...item, [field]: val }
    }))
  }

  function handleXmlFile(e: React.ChangeEvent<HTMLInputElement>, id: number) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = ev => uploadXml.mutate({ id, xml: ev.target?.result as string })
    reader.readAsText(file)
    e.target.value = ''
  }

  const totalForm = itens.reduce((s, i) => s + parseNum(i.quantidade) * parseNum(i.valorUnitario), 0)
  const canSave = itens.some(i => parseNum(i.quantidade) > 0 && parseNum(i.valorUnitario) > 0)
  const nfXml = notas.find(n => n.id === xmlViewId)

  return (
    <div className="p-6 max-w-5xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <FileText size={24} className="text-red-600" /> Notas Fiscais de Saída
          </h1>
          <p className="text-gray-500 text-sm mt-0.5">Faturamento e emissão de NF</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700"
        >
          <Plus size={16} /> Nova NF
        </button>
      </div>

      {/* Lista */}
      <div className="space-y-2">
        {isLoading && <p className="text-gray-400 text-sm">Carregando...</p>}
        {notas.map(nf => (
          <div key={nf.id} className="bg-white rounded-xl border overflow-hidden">
            <div
              className="flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-gray-50"
              onClick={() => setExpandId(expandId === nf.id ? null : nf.id)}
            >
              <div className="flex-1 grid grid-cols-5 gap-3">
                <div>
                  <p className="text-xs text-gray-400">NF / Série</p>
                  <p className="font-semibold text-sm text-gray-800">{nf.numeroNf ? `${nf.numeroNf}/${nf.serieNf}` : 'S/N'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Cliente</p>
                  <p className="text-sm text-gray-700">{nf.cliente?.nome ?? '—'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Emissão</p>
                  <p className="text-sm text-gray-700">{new Date(nf.dataEmissao).toLocaleDateString('pt-BR')}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Total</p>
                  <p className="text-sm font-medium text-gray-800">{brl(nf.valorTotal)}</p>
                </div>
                <div className="flex items-center">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COR[nf.status] ?? 'bg-gray-100 text-gray-600'}`}>
                    {nf.status}
                  </span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {nf.xmlNf ? (
                  <button
                    onClick={e => { e.stopPropagation(); setXmlViewId(nf.id) }}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100"
                  >
                    <FileText size={12} /> XML
                  </button>
                ) : (
                  <label
                    onClick={e => e.stopPropagation()}
                    className="flex items-center gap-1 px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 cursor-pointer"
                  >
                    <Upload size={12} /> XML
                    <input type="file" accept=".xml" className="hidden" onChange={e => handleXmlFile(e, nf.id)} />
                  </label>
                )}
                {nf.status === 'PENDENTE' && (
                  <button
                    onClick={e => { e.stopPropagation(); atualizarStatus.mutate({ id: nf.id, status: 'EMITIDA' }) }}
                    className="p-1.5 rounded hover:bg-emerald-50 text-gray-400 hover:text-emerald-600"
                    title="Marcar como Emitida"
                  >
                    <CheckCircle size={16} />
                  </button>
                )}
                {nf.status !== 'CANCELADA' && (
                  <button
                    onClick={e => { e.stopPropagation(); atualizarStatus.mutate({ id: nf.id, status: 'CANCELADA' }) }}
                    className="p-1.5 rounded hover:bg-red-50 text-gray-400 hover:text-red-500"
                    title="Cancelar NF"
                  >
                    <XCircle size={16} />
                  </button>
                )}
                {expandId === nf.id ? <ChevronUp size={16} className="text-gray-400" /> : <ChevronDown size={16} className="text-gray-400" />}
              </div>
            </div>

            {expandId === nf.id && (
              <div className="border-t bg-gray-50 px-5 py-4">
                <p className="text-xs text-gray-500 mb-3">{nf.naturezaOperacao}</p>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs text-gray-500 uppercase">
                      <th className="text-left pb-2">Descrição</th>
                      <th className="text-right pb-2">Qtd</th>
                      <th className="text-right pb-2">Unit.</th>
                      <th className="text-right pb-2">Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {nf.itens.map(it => (
                      <tr key={it.id} className="border-t border-gray-200">
                        <td className="py-2">{it.descricao}</td>
                        <td className="py-2 text-right">{it.quantidade}</td>
                        <td className="py-2 text-right">{brl(it.valorUnitario)}</td>
                        <td className="py-2 text-right font-medium">{brl(it.valorTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className="border-t-2 border-gray-300 font-semibold">
                      <td colSpan={3} className="pt-2 text-right text-xs uppercase text-gray-500">Total NF</td>
                      <td className="pt-2 text-right">{brl(nf.valorTotal)}</td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            )}
          </div>
        ))}
        {!isLoading && notas.length === 0 && (
          <div className="bg-white rounded-xl border p-10 text-center text-gray-400">
            Nenhuma nota fiscal emitida
          </div>
        )}
      </div>

      {/* XML Viewer */}
      {xmlViewId && nfXml && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl w-full max-w-4xl h-[80vh] flex flex-col">
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-700">
              <div>
                <p className="font-semibold text-white">XML da NF-e</p>
                <p className="text-xs text-gray-400">NF {nfXml.numeroNf ?? 'S/N'} — {nfXml.cliente?.nome ?? 'Sem cliente'}</p>
              </div>
              <div className="flex items-center gap-3">
                <label className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg cursor-pointer hover:bg-blue-700">
                  <Upload size={12} /> Atualizar
                  <input type="file" accept=".xml" className="hidden" onChange={e => handleXmlFile(e, xmlViewId)} />
                </label>
                <button onClick={() => setXmlViewId(null)} className="text-gray-400 hover:text-white">
                  <X size={20} />
                </button>
              </div>
            </div>
            <pre className="flex-1 overflow-auto p-5 text-xs text-green-400 font-mono whitespace-pre-wrap">
              {nfXml.xmlNf}
            </pre>
          </div>
        </div>
      )}

      {/* Modal Nova NF */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-start justify-center z-50 p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl w-full max-w-2xl my-4 p-6 space-y-5">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold">Nova Nota Fiscal de Saída</h2>
              <button onClick={resetForm}><X size={20} className="text-gray-400 hover:text-gray-700" /></button>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">Cliente</label>
                <select value={clienteId} onChange={e => setClienteId(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="">Sem cliente (consumidor final)</option>
                  {clientes.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Número NF</label>
                <input value={numeroNf} onChange={e => setNumeroNf(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm" placeholder="000001" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600">Série</label>
                <input value={serieNf} onChange={e => setSerieNf(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm" placeholder="1" />
              </div>
              <div className="col-span-2">
                <label className="text-xs font-medium text-gray-600">Natureza da Operação</label>
                <input value={natureza} onChange={e => setNatureza(e.target.value)}
                  className="mt-1 w-full border rounded-lg px-3 py-2 text-sm" />
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
                <label className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Itens</label>
                <button onClick={addItem} className="flex items-center gap-1 text-xs text-red-600 hover:text-red-700">
                  <Plus size={12} /> Adicionar
                </button>
              </div>
              <div className="space-y-2">
                {itens.map((item, idx) => (
                  <div key={idx} className="grid grid-cols-12 gap-2 items-end bg-gray-50 rounded-lg p-3">
                    <div className="col-span-4">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Produto</label>}
                      <select
                        value={item.produtoId ?? ''}
                        onChange={e => updateItem(idx, 'produtoId', e.target.value ? parseInt(e.target.value) : null)}
                        className="w-full border rounded px-2 py-1.5 text-sm bg-white"
                      >
                        <option value="">Outro</option>
                        {produtos.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
                      </select>
                    </div>
                    <div className="col-span-3">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Descrição</label>}
                      <input
                        value={item.descricao}
                        onChange={e => updateItem(idx, 'descricao', e.target.value)}
                        placeholder="Descrição"
                        className="w-full border rounded px-2 py-1.5 text-sm"
                      />
                    </div>
                    <div className="col-span-2">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">Qtd</label>}
                      <input
                        value={item.quantidade}
                        onChange={e => updateItem(idx, 'quantidade', e.target.value)}
                        placeholder="0,000"
                        className="w-full border rounded px-2 py-1.5 text-sm"
                      />
                    </div>
                    <div className="col-span-2">
                      {idx === 0 && <label className="text-xs text-gray-500 block mb-1">R$/un</label>}
                      <input
                        value={item.valorUnitario}
                        onChange={e => updateItem(idx, 'valorUnitario', e.target.value)}
                        placeholder="0,00"
                        className="w-full border rounded px-2 py-1.5 text-sm"
                      />
                    </div>
                    <div className="col-span-1 flex justify-center">
                      {itens.length > 1 && (
                        <button onClick={() => removeItem(idx)} className="text-red-400 hover:text-red-600">
                          <X size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
              {totalForm > 0 && (
                <p className="text-right text-sm font-semibold text-gray-700 mt-2">
                  Total: {brl(totalForm)}
                </p>
              )}
            </div>

            <div className="flex gap-3 pt-2">
              <button onClick={resetForm} className="flex-1 py-2.5 border rounded-lg text-sm text-gray-600">
                Cancelar
              </button>
              <button
                onClick={() => criar.mutate()}
                disabled={!canSave || criar.isPending}
                className="flex-1 py-2.5 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50"
              >
                {criar.isPending ? 'Criando...' : 'Criar NF'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
