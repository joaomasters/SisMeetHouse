import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ShieldAlert, Filter, Plus, Pencil, Trash2 } from 'lucide-react'
import { api } from '@/shared/api/axios'
import type { LogAuditoria, ModuloInfo, Usuario } from '@/types/acesso'

const hoje = new Date().toISOString().slice(0, 10)
const seteDiasAtras = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)

const acaoConfig: Record<string, { label: string; cor: string; icone: typeof Plus }> = {
  CRIAR:   { label: 'Criou',    cor: 'bg-emerald-100 text-emerald-700', icone: Plus },
  EDITAR:  { label: 'Editou',   cor: 'bg-blue-100 text-blue-700',       icone: Pencil },
  EXCLUIR: { label: 'Excluiu',  cor: 'bg-red-100 text-red-700',         icone: Trash2 },
}

export default function AuditoriaPage() {
  const [filtroInicio, setFiltroInicio]     = useState(seteDiasAtras)
  const [filtroFim, setFiltroFim]           = useState(hoje)
  const [filtroModulo, setFiltroModulo]     = useState('')
  const [filtroAcao, setFiltroAcao]         = useState('')
  const [filtroUsuarioId, setFiltroUsuarioId] = useState('')

  const { data: modulos = [] } = useQuery<ModuloInfo[]>({
    queryKey: ['modulos'],
    queryFn: () => api.get('/perfis/modulos').then(r => r.data),
  })
  const rotuloDoModulo = (nome: string) => modulos.find(m => m.nome === nome)?.rotulo ?? nome

  const { data: usuarios = [] } = useQuery<Usuario[]>({
    queryKey: ['usuarios'],
    queryFn: () => api.get('/usuarios').then(r => r.data),
  })

  const { data: logs = [], isLoading } = useQuery<LogAuditoria[]>({
    queryKey: ['auditoria', filtroInicio, filtroFim, filtroModulo, filtroAcao, filtroUsuarioId],
    queryFn: () => api.get('/auditoria', {
      params: {
        inicio: filtroInicio || undefined,
        fim: filtroFim || undefined,
        modulo: filtroModulo || undefined,
        acao: filtroAcao || undefined,
        usuarioId: filtroUsuarioId || undefined,
      },
    }).then(r => r.data),
  })

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <ShieldAlert size={24} className="text-red-600" /> Auditoria
        </h1>
        <p className="text-gray-500 text-sm">
          Histórico de ações administrativas — quem criou, editou ou excluiu o quê, e quando.
        </p>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-xl border p-4 mb-4 flex flex-wrap items-end gap-3">
        <Filter size={16} className="text-gray-400 mb-2" />
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">De</label>
          <input type="date" value={filtroInicio} onChange={e => setFiltroInicio(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm" />
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Até</label>
          <input type="date" value={filtroFim} onChange={e => setFiltroFim(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm" />
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Módulo</label>
          <select value={filtroModulo} onChange={e => setFiltroModulo(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm min-w-[160px]">
            <option value="">Todos</option>
            {modulos.map(m => <option key={m.nome} value={m.nome}>{m.rotulo}</option>)}
          </select>
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Ação</label>
          <select value={filtroAcao} onChange={e => setFiltroAcao(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm">
            <option value="">Todas</option>
            <option value="CRIAR">Criou</option>
            <option value="EDITAR">Editou</option>
            <option value="EXCLUIR">Excluiu</option>
          </select>
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600 block mb-1">Usuário</label>
          <select value={filtroUsuarioId} onChange={e => setFiltroUsuarioId(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm min-w-[160px]">
            <option value="">Todos</option>
            {usuarios.map(u => <option key={u.id} value={u.id}>{u.nome}</option>)}
          </select>
        </div>
      </div>

      {/* Lista */}
      <div className="bg-white rounded-xl border overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-400 text-sm">Carregando...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Quando</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Usuário</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Módulo</th>
                <th className="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Ação</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">O quê</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {logs.map(l => {
                const cfg = acaoConfig[l.acao] ?? acaoConfig.EDITAR
                const Icone = cfg.icone
                return (
                  <tr key={l.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500 text-xs whitespace-nowrap">
                      {new Date(l.criadoEm).toLocaleString('pt-BR', {
                        day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
                      })}
                    </td>
                    <td className="px-4 py-3">
                      <span className="font-medium">{l.nomeUsuario}</span>
                      {l.perfilUsuario && (
                        <span className="text-xs text-gray-400 ml-1.5">({l.perfilUsuario})</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-gray-600">{rotuloDoModulo(l.modulo)}</td>
                    <td className="px-4 py-3 text-center">
                      <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${cfg.cor}`}>
                        <Icone size={11} /> {cfg.label}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500 font-mono text-xs">{l.descricao ?? '—'}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
        {!isLoading && logs.length === 0 && (
          <p className="text-center text-gray-400 py-10 text-sm">
            Nenhuma ação registrada para os filtros selecionados.
          </p>
        )}
      </div>
    </div>
  )
}