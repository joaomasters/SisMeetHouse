import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Scale, Download, Eye } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Produto } from '@/types/produto'

type TipoBalanca = 'TOLEDO_MGV7' | 'FILIZOLA_SMART'

const BALANÇAS = [
  { value: 'TOLEDO_MGV7'    as TipoBalanca, label: 'Toledo MGV6/MGV7',  desc: 'Formato PLU com cabeçalho 99|CARGA|1|1' },
  { value: 'FILIZOLA_SMART' as TipoBalanca, label: 'Filizola Smart',    desc: 'Formato CSV posicional 1;PLU;NOME;PRECO;VALIDADE' },
]

export default function CargaBalancaPage() {
  const [tipo, setTipo]         = useState<TipoBalanca>('TOLEDO_MGV7')
  const [preview, setPreview]   = useState<string | null>(null)
  const [loadPreview, setLoadP] = useState(false)
  const [downloading, setDown]  = useState(false)

  const { data: produtos = [] } = useQuery<Produto[]>({
    queryKey: ['produtos-balanca'],
    queryFn: () => api.get('/estoque/produtos', { params: { balanca: true } }).then(r => r.data),
  })

  const prodComPlu = produtos.filter(p => p.codigoBalanca)

  async function verPreview() {
    setLoadP(true)
    try {
      const path = tipo === 'TOLEDO_MGV7' ? 'toledo-mgv7' : 'filizola-smart'
      const { data } = await api.get<string>(`/balanca/carga/preview/${path}`)
      setPreview(data)
    } finally {
      setLoadP(false)
    }
  }

  async function baixarArquivo() {
    setDown(true)
    try {
      const path = tipo === 'TOLEDO_MGV7' ? 'toledo-mgv7' : 'filizola-smart'
      const resp = await api.get(`/balanca/carga/${path}`, { responseType: 'blob' })
      const url  = URL.createObjectURL(new Blob([resp.data]))
      const a    = document.createElement('a')
      a.href     = url
      a.download = tipo === 'TOLEDO_MGV7' ? 'carga_toledo.txt' : 'carga_filizola.txt'
      a.click()
      URL.revokeObjectURL(url)
      toast.success('Arquivo gerado e registrado!')
    } finally {
      setDown(false)
    }
  }

  return (
    <div className="p-6 max-w-3xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Scale size={24} className="text-red-600" /> Carga de Balança
        </h1>
        <p className="text-gray-500 text-sm mt-0.5">
          Gera arquivo de atualização de preços e cadastro de PLUs para balanças
        </p>
      </div>

      {/* Seletor de balança */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        {BALANÇAS.map(b => (
          <button
            key={b.value}
            onClick={() => { setTipo(b.value); setPreview(null) }}
            className={`p-4 rounded-xl border-2 text-left transition-all
              ${tipo === b.value
                ? 'border-red-500 bg-red-50'
                : 'border-gray-200 bg-white hover:border-gray-300'}`}
          >
            <p className="font-semibold text-gray-900">{b.label}</p>
            <p className="text-xs text-gray-500 mt-1">{b.desc}</p>
          </button>
        ))}
      </div>

      {/* Resumo de produtos com PLU */}
      <div className="bg-white rounded-xl shadow p-5 mb-6">
        <div className="flex items-center justify-between mb-3">
          <p className="font-medium text-gray-900">
            Produtos com PLU cadastrado: <span className="text-red-600">{prodComPlu.length}</span>
          </p>
          <div className="flex gap-2">
            <button
              onClick={verPreview}
              disabled={loadPreview}
              className="flex items-center gap-2 px-3 py-1.5 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
            >
              <Eye size={14} />
              {loadPreview ? 'Carregando...' : 'Pré-visualizar'}
            </button>
            <button
              onClick={baixarArquivo}
              disabled={downloading || prodComPlu.length === 0}
              className="flex items-center gap-2 px-4 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
            >
              <Download size={14} />
              {downloading ? 'Gerando...' : 'Baixar Arquivo'}
            </button>
          </div>
        </div>

        <div className="max-h-48 overflow-y-auto">
          <table className="w-full text-xs">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left text-gray-500">PLU</th>
                <th className="px-3 py-2 text-left text-gray-500">Nome</th>
                <th className="px-3 py-2 text-right text-gray-500">Preço/KG</th>
                <th className="px-3 py-2 text-center text-gray-500">EAN-13</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {prodComPlu.map(p => (
                <tr key={p.id}>
                  <td className="px-3 py-2 font-mono">{String(p.codigoBalanca).padStart(5, '0')}</td>
                  <td className="px-3 py-2">{p.nome}</td>
                  <td className="px-3 py-2 text-right tabular-nums font-medium">
                    {p.precoVenda.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                  </td>
                  <td className="px-3 py-2 text-center font-mono text-gray-400">
                    {p.codigoBalanca ? `2${String(p.codigoBalanca).padStart(5, '0')}XXXXX` : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pré-visualização do arquivo */}
      {preview && (
        <div className="bg-gray-900 rounded-xl p-4">
          <div className="flex items-center justify-between mb-2">
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">
              Pré-visualização — {tipo}
            </p>
            <button onClick={() => setPreview(null)} className="text-gray-500 hover:text-gray-300 text-xs">
              Fechar
            </button>
          </div>
          <pre className="text-green-400 text-xs font-mono whitespace-pre overflow-x-auto max-h-64">
            {preview}
          </pre>
        </div>
      )}
    </div>
  )
}
