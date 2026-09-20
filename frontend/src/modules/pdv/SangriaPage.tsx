import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/axios'
import { getUsuarioId, getNomeUsuario } from '../../shared/auth'
import { ArrowDownCircle, ArrowUpCircle, Plus, Wallet, AlertCircle, History, Lock, LockOpen, User } from 'lucide-react'

interface CaixaAberto {
  id: number
  operadorId: number
  dataAbertura: string
  valorAbertura: number
}

interface CaixaHistorico {
  id: number
  operadorId: number
  dataAbertura: string
  dataFechamento: string | null
  valorAbertura: number
  valorFechamentoInformado: number | null
  status: 'ABERTO' | 'FECHADO'
}

interface SangriaItem {
  id: number
  tipo: 'SANGRIA' | 'SUPRIMENTO'
  valor: number
  motivo: string
  createdAt: string
}

interface FechamentoDTO {
  caixaId: number
  valorAbertura: number
  totalSangria: number
  totalSuprimento: number
  totalVendas: number
  totalDinheiro: number
  totalCredito: number
  totalDebito: number
  totalPix: number
  totalFiado: number
  saldoEsperado: number
  quantidadeVendas: number
  movimentos: SangriaItem[]
}

const fmt = (v: number) => `R$ ${(v ?? 0).toFixed(2).replace('.', ',')}`

export default function SangriaPage() {
  const qc = useQueryClient()
  const [tipo, setTipo] = useState<'SANGRIA' | 'SUPRIMENTO'>('SANGRIA')
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')
  const [showFechamento, setShowFechamento] = useState(false)
  const [fechamento, setFechamento] = useState<FechamentoDTO | null>(null)
  const [showHistorico, setShowHistorico] = useState(false)
  const [valorAbertura, setValorAbertura] = useState('')

  const operadorId = getUsuarioId()
  const nomeOperador = getNomeUsuario()

  // A tela agora descobre sozinha qual é o caixa aberto DO OPERADOR LOGADO
  // — cada operador tem seu próprio caixa; não existe mais "o caixa aberto
  // do sistema" genérico. Isso evita registrar sangria no caixa errado
  // quando há mais de um operador trabalhando ao mesmo tempo.
  const caixaQuery = useQuery<CaixaAberto>({
    queryKey: ['caixa-aberto', operadorId],
    queryFn: () => api.get('/pdv/caixa/aberto', { params: { operadorId }, silent: true } as any).then(r => r.data),
    enabled: !!operadorId,
    retry: false,
  })
  const caixaId = caixaQuery.data?.id

  // Nomes dos operadores, pra mostrar no histórico de caixas (que pode
  // incluir caixas de outros operadores, não só o do usuário logado).
  const { data: usuarios = [] } = useQuery<{ id: number; nome: string }[]>({
    queryKey: ['usuarios-nomes'],
    queryFn: () => api.get('/usuarios').then(r => r.data),
  })
  const nomeDoOperador = (id: number) => usuarios.find(u => u.id === id)?.nome ?? `Operador #${id}`

  // Histórico de todas as sessões de caixa já abertas — responde "quantos
  // caixas existem" sem precisar ir direto no banco.
  const historicoQuery = useQuery<CaixaHistorico[]>({
    queryKey: ['caixas-historico'],
    queryFn: () => api.get('/pdv/caixa').then(r => r.data),
  })

  const abrirCaixa = useMutation({
    mutationFn: () => api.post('/pdv/caixa/abrir', null, {
      params: { operadorId, valorAbertura: parseFloat(valorAbertura.replace(',', '.')) },
    }),
    onSuccess: () => {
      setValorAbertura('')
      qc.invalidateQueries({ queryKey: ['caixa-aberto'] })
      qc.invalidateQueries({ queryKey: ['caixas-historico'] })
    },
  })

  const movQuery = useQuery<SangriaItem[]>({
    queryKey: ['sangria', caixaId],
    queryFn: () => api.get(`/pdv/caixa/${caixaId}/movimentos`).then(r => r.data),
    enabled: !!caixaId,
  })

  const registrar = useMutation({
    mutationFn: () => api.post(`/pdv/caixa/${caixaId}/${tipo.toLowerCase()}`, {
      valor: parseFloat(valor.replace(',', '.')),
      motivo,
      operadorId,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['sangria', caixaId] })
      setValor('')
      setMotivo('')
    },
  })

  const verFechamento = async () => {
    const r = await api.get(`/pdv/caixa/${caixaId}/fechamento`)
    setFechamento(r.data)
    setShowFechamento(true)
  }

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Sangria / Suprimento</h1>
          <p className="text-sm text-gray-500">Movimentações de numerário no caixa</p>
        </div>
      </div>

      {/* Identificação do caixa — automática, sem precisar saber o ID */}
      {caixaQuery.isLoading && (
        <div className="bg-white rounded-xl border p-4 text-sm text-gray-400">Verificando caixa aberto...</div>
      )}

      {caixaQuery.isError && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 space-y-3">
          <div className="flex items-start gap-3">
            <AlertCircle size={18} className="text-amber-600 shrink-0 mt-0.5" />
            <div>
              <p className="text-sm font-medium text-amber-800">Nenhum caixa aberto no momento</p>
              <p className="text-xs text-amber-700 mt-0.5">
                Informe o valor inicial (fundo de troco) pra abrir um caixa novo.
              </p>
            </div>
          </div>
          <div className="flex gap-2 pl-8">
            <input
              type="text"
              value={valorAbertura}
              onChange={e => setValorAbertura(e.target.value)}
              placeholder="0,00"
              className="flex-1 border border-amber-300 rounded-lg px-3 py-2 text-sm bg-white"
            />
            <button
              onClick={() => abrirCaixa.mutate()}
              disabled={!valorAbertura || abrirCaixa.isPending}
              className="flex items-center gap-2 px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-sm font-medium disabled:opacity-50"
            >
              <LockOpen size={14} /> {abrirCaixa.isPending ? 'Abrindo...' : 'Abrir Caixa'}
            </button>
          </div>
        </div>
      )}

      {caixaQuery.data && (
        <div className="bg-white rounded-xl border p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center shrink-0">
            <Wallet size={18} className="text-emerald-600" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-800">
              Caixa #{caixaQuery.data.id} aberto às{' '}
              {new Date(caixaQuery.data.dataAbertura).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-xs text-gray-500 flex items-center gap-1">
              <User size={11} /> {nomeOperador ?? `Operador #${caixaQuery.data.operadorId}`}
              <span className="mx-1">•</span>
              Abertura: {fmt(caixaQuery.data.valorAbertura)}
            </p>
          </div>
          <button
            onClick={verFechamento}
            className="px-4 py-2 text-sm bg-gray-800 text-white rounded-lg hover:bg-gray-700 shrink-0"
          >
            Ver Fechamento
          </button>
        </div>
      )}

      {/* Quantos caixas já existem — histórico de todas as sessões */}
      <div className="bg-white rounded-xl border overflow-hidden">
        <button
          onClick={() => setShowHistorico(s => !s)}
          className="w-full flex items-center justify-between px-4 py-3 text-sm font-medium text-gray-600 hover:bg-gray-50"
        >
          <span className="flex items-center gap-2">
            <History size={15} />
            Histórico de caixas
            {historicoQuery.data && (
              <span className="text-xs font-normal text-gray-400">
                ({historicoQuery.data.length} no total)
              </span>
            )}
          </span>
          <span className="text-xs text-gray-400">{showHistorico ? 'Ocultar' : 'Ver todos'}</span>
        </button>

        {showHistorico && (
          <table className="w-full text-sm border-t">
            <thead>
              <tr className="text-xs text-gray-400 border-b bg-gray-50">
                <th className="px-4 py-2 text-left">Caixa</th>
                <th className="px-4 py-2 text-left">Operador</th>
                <th className="px-4 py-2 text-left">Abertura</th>
                <th className="px-4 py-2 text-left">Fechamento</th>
                <th className="px-4 py-2 text-right">Valor Abertura</th>
                <th className="px-4 py-2 text-center">Status</th>
              </tr>
            </thead>
            <tbody>
              {historicoQuery.data?.map(c => (
                <tr key={c.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">#{c.id}</td>
                  <td className="px-4 py-3 text-gray-600">{nomeDoOperador(c.operadorId)}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">
                    {new Date(c.dataAbertura).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs">
                    {c.dataFechamento
                      ? new Date(c.dataFechamento).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
                      : '—'}
                  </td>
                  <td className="px-4 py-3 text-right tabular-nums">{fmt(c.valorAbertura)}</td>
                  <td className="px-4 py-3 text-center">
                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium
                      ${c.status === 'ABERTO' ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-100 text-gray-500'}`}>
                      {c.status === 'ABERTO' ? <LockOpen size={10} /> : <Lock size={10} />}
                      {c.status}
                    </span>
                  </td>
                </tr>
              ))}
              {historicoQuery.data?.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-6 text-center text-gray-400">Nenhum caixa foi aberto ainda.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Formulário */}
      {caixaId && (
        <div className="bg-white rounded-xl border p-5 space-y-4">
          <div className="flex gap-3">
            {(['SANGRIA', 'SUPRIMENTO'] as const).map(t => (
              <button
                key={t}
                onClick={() => setTipo(t)}
                className={`flex-1 py-2.5 rounded-lg text-sm font-medium flex items-center justify-center gap-2 border transition
                  ${tipo === t
                    ? t === 'SANGRIA' ? 'bg-red-600 text-white border-red-600' : 'bg-green-600 text-white border-green-600'
                    : 'text-gray-600 border-gray-200 hover:bg-gray-50'}`}
              >
                {t === 'SANGRIA' ? <ArrowDownCircle size={16} /> : <ArrowUpCircle size={16} />}
                {t}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-500 font-medium">Valor (R$)</label>
              <input
                type="text"
                value={valor}
                onChange={e => setValor(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm"
                placeholder="0,00"
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 font-medium">Motivo</label>
              <input
                type="text"
                value={motivo}
                onChange={e => setMotivo(e.target.value)}
                className="mt-1 block w-full border rounded-lg px-3 py-2 text-sm"
                placeholder="Opcional"
              />
            </div>
          </div>

          <button
            onClick={() => registrar.mutate()}
            disabled={!valor || registrar.isPending}
            className="w-full py-2.5 bg-red-600 text-white rounded-lg text-sm font-medium flex items-center justify-center gap-2 hover:bg-red-700 disabled:opacity-50"
          >
            <Plus size={16} />
            Registrar {tipo === 'SANGRIA' ? 'Sangria' : 'Suprimento'}
          </button>
        </div>
      )}

      {/* Lista de movimentos */}
      {movQuery.data && movQuery.data.length > 0 && (
        <div className="bg-white rounded-xl border overflow-hidden">
          <div className="px-5 py-3 border-b bg-gray-50 text-sm font-medium text-gray-600">
            Movimentos do Caixa
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs text-gray-400 border-b">
                <th className="px-4 py-2 text-left">Tipo</th>
                <th className="px-4 py-2 text-left">Valor</th>
                <th className="px-4 py-2 text-left">Motivo</th>
                <th className="px-4 py-2 text-left">Horário</th>
              </tr>
            </thead>
            <tbody>
              {movQuery.data.map(m => (
                <tr key={m.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium
                      ${m.tipo === 'SANGRIA' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                      {m.tipo === 'SANGRIA' ? <ArrowDownCircle size={12} /> : <ArrowUpCircle size={12} />}
                      {m.tipo}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-medium">{fmt(m.valor)}</td>
                  <td className="px-4 py-3 text-gray-500">{m.motivo || '—'}</td>
                  <td className="px-4 py-3 text-gray-400">
                    {new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Fechamento */}
      {showFechamento && fechamento && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4">
            <h2 className="text-lg font-bold">Fechamento do Caixa #{fechamento.caixaId}</h2>
            <div className="space-y-2 text-sm">
              {[
                ['Abertura', fechamento.valorAbertura],
                ['Vendas', fechamento.totalVendas],
                ['Dinheiro', fechamento.totalDinheiro],
                ['Crédito', fechamento.totalCredito],
                ['Débito', fechamento.totalDebito],
                ['PIX', fechamento.totalPix],
                ['Fiado', fechamento.totalFiado],
                ['Suprimento (+)', fechamento.totalSuprimento],
                ['Sangria (-)', fechamento.totalSangria],
              ].map(([label, val]) => (
                <div key={label as string} className="flex justify-between border-b pb-1">
                  <span className="text-gray-500">{label}</span>
                  <span className="font-medium">{fmt(val as number)}</span>
                </div>
              ))}
              <div className="flex justify-between pt-2 text-base font-bold">
                <span>Saldo Esperado em Caixa</span>
                <span className="text-green-600">{fmt(fechamento.saldoEsperado)}</span>
              </div>
              <p className="text-xs text-gray-400">{fechamento.quantidadeVendas} vendas no período</p>
            </div>
            <button
              onClick={() => setShowFechamento(false)}
              className="w-full py-2 bg-gray-800 text-white rounded-lg text-sm"
            >
              Fechar
            </button>
          </div>
        </div>
      )}
    </div>
  )
}