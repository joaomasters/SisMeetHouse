import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Users, Plus, KeyRound, Power, Lock, X } from 'lucide-react'
import { api } from '@/shared/api/axios'
import { usePermissao } from '@/shared/hooks/usePermissao'
import toast from 'react-hot-toast'
import type { Usuario, Perfil } from '@/types/acesso'

export default function UsuariosPage() {
  const qc = useQueryClient()
  const { podeVer, podeCriar, podeEditar, podeExcluir } = usePermissao()

  const [showNovo, setShowNovo]   = useState(false)
  const [nome, setNome]           = useState('')
  const [login, setLogin]         = useState('')
  const [senha, setSenha]         = useState('')
  const [perfilId, setPerfilId]   = useState('')
  const [trocandoSenhaId, setTrocandoSenhaId] = useState<number | null>(null)
  const [novaSenha, setNovaSenha] = useState('')

  const { data: usuarios = [] } = useQuery<Usuario[]>({
    queryKey: ['usuarios'],
    queryFn: () => api.get('/usuarios').then(r => r.data),
    enabled: podeVer('USUARIOS'),
  })

  const { data: perfis = [] } = useQuery<Perfil[]>({
    queryKey: ['perfis'],
    queryFn: () => api.get('/perfis').then(r => r.data),
    enabled: podeVer('USUARIOS'),
  })

  const criar = useMutation({
    mutationFn: () => api.post('/usuarios', { nome, login, senha, perfilId: Number(perfilId) }),
    onSuccess: () => {
      toast.success('Usuário criado!')
      qc.invalidateQueries({ queryKey: ['usuarios'] })
      setNome(''); setLogin(''); setSenha(''); setPerfilId(''); setShowNovo(false)
    },
  })

  const trocarPerfil = useMutation({
    mutationFn: ({ id, nome, perfilId }: { id: number; nome: string; perfilId: number }) =>
      api.put(`/usuarios/${id}`, { nome, perfilId }),
    onSuccess: () => {
      toast.success('Perfil do usuário atualizado.')
      qc.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })

  const alterarStatus = useMutation({
    mutationFn: ({ id, ativo }: { id: number; ativo: boolean }) =>
      api.patch(`/usuarios/${id}/status`, { ativo }),
    onSuccess: () => {
      toast.success('Status atualizado.')
      qc.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })

  const trocarSenha = useMutation({
    mutationFn: (id: number) => api.put(`/usuarios/${id}/senha`, { novaSenha }),
    onSuccess: () => {
      toast.success('Senha alterada.')
      setTrocandoSenhaId(null)
      setNovaSenha('')
    },
  })

  if (!podeVer('USUARIOS')) {
    return (
      <div className="p-6">
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-5 max-w-lg">
          <p className="flex items-center gap-2 font-medium text-amber-800">
            <Lock size={16} /> Acesso restrito
          </p>
          <p className="text-sm text-amber-700 mt-1">
            Seu perfil não tem permissão para visualizar os usuários do sistema.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Users size={24} className="text-red-600" /> Usuários
          </h1>
          <p className="text-gray-500 text-sm mt-0.5">Contas de acesso ao sistema e seus perfis</p>
        </div>
        {podeCriar('USUARIOS') && (
          <button
            onClick={() => setShowNovo(v => !v)}
            className="flex items-center gap-1.5 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium"
          >
            <Plus size={16} /> Novo Usuário
          </button>
        )}
      </div>

      {showNovo && podeCriar('USUARIOS') && (
        <div className="bg-white rounded-xl shadow p-4 mb-5 grid grid-cols-1 md:grid-cols-5 gap-3 items-end">
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Nome *</label>
            <input value={nome} onChange={e => setNome(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Login *</label>
            <input value={login} onChange={e => setLogin(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Senha *</label>
            <input type="password" value={senha} onChange={e => setSenha(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-600 block mb-1">Perfil *</label>
            <select value={perfilId} onChange={e => setPerfilId(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
              <option value="">Selecione...</option>
              {perfis.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
            </select>
          </div>
          <button
            onClick={() => criar.mutate()}
            disabled={!nome || !login || !senha || !perfilId || criar.isPending}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
          >
            Criar
          </button>
        </div>
      )}

      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Nome</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Login</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Perfil</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Status</th>
              <th className="px-4 py-3 w-40" />
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {usuarios.map(u => (
              <tr key={u.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{u.nome}</td>
                <td className="px-4 py-3 text-gray-500 font-mono text-xs">{u.login}</td>
                <td className="px-4 py-3">
                  {podeEditar('USUARIOS') ? (
                    <select
                      value={u.perfil.id}
                      onChange={e => trocarPerfil.mutate({ id: u.id, nome: u.nome, perfilId: Number(e.target.value) })}
                      className="border border-gray-300 rounded px-2 py-1 text-xs"
                    >
                      {perfis.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
                    </select>
                  ) : (
                    <span className="text-gray-600">{u.perfil.nome}</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium
                    ${u.ativo ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-200 text-gray-500'}`}>
                    {u.ativo ? 'Ativo' : 'Inativo'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right space-x-1">
                  {podeEditar('USUARIOS') && (
                    <button
                      onClick={() => setTrocandoSenhaId(u.id)}
                      title="Trocar senha"
                      className="p-1.5 text-gray-400 hover:text-blue-600 rounded"
                    >
                      <KeyRound size={15} />
                    </button>
                  )}
                  {podeExcluir('USUARIOS') && (
                    <button
                      onClick={() => alterarStatus.mutate({ id: u.id, ativo: !u.ativo })}
                      title={u.ativo ? 'Desativar' : 'Reativar'}
                      className="p-1.5 text-gray-400 hover:text-red-600 rounded"
                    >
                      <Power size={15} />
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {usuarios.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">Nenhum usuário cadastrado.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal de troca de senha */}
      {trocandoSenhaId !== null && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-5 w-full max-w-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-bold text-gray-900">Trocar senha</h2>
              <button onClick={() => { setTrocandoSenhaId(null); setNovaSenha('') }}
                className="text-gray-400 hover:text-gray-700">
                <X size={18} />
              </button>
            </div>
            <input
              type="password"
              autoFocus
              value={novaSenha}
              onChange={e => setNovaSenha(e.target.value)}
              placeholder="Nova senha (mín. 4 caracteres)"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-4"
            />
            <button
              onClick={() => trocarSenha.mutate(trocandoSenhaId)}
              disabled={novaSenha.length < 4 || trocarSenha.isPending}
              className="w-full py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
            >
              {trocarSenha.isPending ? 'Salvando...' : 'Salvar nova senha'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}