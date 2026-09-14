import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ShieldCheck, Plus, Save, Trash2, Lock } from 'lucide-react'
import { api } from '@/shared/api/axios'
import { usePermissao } from '@/shared/hooks/usePermissao'
import toast from 'react-hot-toast'
import type { Perfil, ModuloInfo } from '@/types/acesso'

type Acao = 'podeVer' | 'podeCriar' | 'podeEditar' | 'podeExcluir'

const ACOES: { campo: Acao; rotulo: string }[] = [
  { campo: 'podeVer',     rotulo: 'Ver' },
  { campo: 'podeCriar',   rotulo: 'Criar' },
  { campo: 'podeEditar',  rotulo: 'Editar' },
  { campo: 'podeExcluir', rotulo: 'Excluir' },
]

export default function PerfisPage() {
  const qc = useQueryClient()
  const { isSuperAdmin } = usePermissao()
  const [perfilSelId, setPerfilSelId] = useState<number | null>(null)
  const [matriz, setMatriz] = useState<Record<string, Record<Acao, boolean>>>({})
  const [novoNome, setNovoNome] = useState('')
  const [novaDescricao, setNovaDescricao] = useState('')
  const [showNovo, setShowNovo] = useState(false)

  const { data: perfis = [] } = useQuery<Perfil[]>({
    queryKey: ['perfis'],
    queryFn: () => api.get('/perfis').then(r => r.data),
  })

  const { data: modulos = [] } = useQuery<ModuloInfo[]>({
    queryKey: ['modulos'],
    queryFn: () => api.get('/perfis/modulos').then(r => r.data),
  })

  const perfilSel = perfis.find(p => p.id === perfilSelId) ?? null

  // Ao trocar de perfil, carrega a matriz dele no estado local
  useEffect(() => {
    if (!perfilSel) return
    const nova: Record<string, Record<Acao, boolean>> = {}
    modulos.forEach(m => {
      const p = perfilSel.permissoes.find(pp => pp.modulo === m.nome)
      nova[m.nome] = {
        podeVer:     p?.podeVer ?? false,
        podeCriar:   p?.podeCriar ?? false,
        podeEditar:  p?.podeEditar ?? false,
        podeExcluir: p?.podeExcluir ?? false,
      }
    })
    setMatriz(nova)
  }, [perfilSelId, perfis, modulos])

  const criarPerfil = useMutation({
    mutationFn: () => api.post('/perfis', { nome: novoNome.trim().toUpperCase(), descricao: novaDescricao }),
    onSuccess: () => {
      toast.success('Perfil criado!')
      qc.invalidateQueries({ queryKey: ['perfis'] })
      setNovoNome(''); setNovaDescricao(''); setShowNovo(false)
    },
  })

  const salvarPermissoes = useMutation({
    mutationFn: () => api.put(`/perfis/${perfilSelId}/permissoes`, {
      permissoes: modulos.map(m => ({
        modulo:  m.nome,
        ver:     matriz[m.nome]?.podeVer     ?? false,
        criar:   matriz[m.nome]?.podeCriar   ?? false,
        editar:  matriz[m.nome]?.podeEditar  ?? false,
        excluir: matriz[m.nome]?.podeExcluir ?? false,
      })),
    }),
    onSuccess: () => {
      toast.success('Permissões salvas! Os usuários desse perfil verão a mudança no próximo login.')
      qc.invalidateQueries({ queryKey: ['perfis'] })
    },
  })

  const excluirPerfil = useMutation({
    mutationFn: (id: number) => api.delete(`/perfis/${id}`),
    onSuccess: () => {
      toast.success('Perfil excluído.')
      qc.invalidateQueries({ queryKey: ['perfis'] })
      setPerfilSelId(null)
    },
  })

  function alternar(modulo: string, acao: Acao) {
    setMatriz(prev => ({
      ...prev,
      [modulo]: { ...prev[modulo], [acao]: !prev[modulo]?.[acao] },
    }))
  }

  function marcarTodos(modulo: string, valor: boolean) {
    setMatriz(prev => ({
      ...prev,
      [modulo]: { podeVer: valor, podeCriar: valor, podeEditar: valor, podeExcluir: valor },
    }))
  }

  if (!isSuperAdmin) {
    return (
      <div className="p-6">
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-5 max-w-lg">
          <p className="flex items-center gap-2 font-medium text-amber-800">
            <Lock size={16} /> Acesso restrito
          </p>
          <p className="text-sm text-amber-700 mt-1">
            Apenas o SUPER_ADMIN pode gerenciar perfis e permissões.
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
            <ShieldCheck size={24} className="text-red-600" /> Perfis de Acesso
          </h1>
          <p className="text-gray-500 text-sm mt-0.5">
            Defina o que cada perfil pode ver e fazer em cada módulo
          </p>
        </div>
        <button
          onClick={() => setShowNovo(v => !v)}
          className="flex items-center gap-1.5 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium"
        >
          <Plus size={16} /> Novo Perfil
        </button>
      </div>

      {showNovo && (
        <div className="bg-white rounded-xl shadow p-4 mb-5 flex gap-3 items-end max-w-2xl">
          <div className="flex-1">
            <label className="text-xs font-medium text-gray-600 block mb-1">Nome *</label>
            <input
              value={novoNome}
              onChange={e => setNovoNome(e.target.value)}
              placeholder="Ex: FISCAL"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm uppercase"
            />
          </div>
          <div className="flex-[2]">
            <label className="text-xs font-medium text-gray-600 block mb-1">Descrição</label>
            <input
              value={novaDescricao}
              onChange={e => setNovaDescricao(e.target.value)}
              placeholder="Para que serve esse perfil"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>
          <button
            onClick={() => criarPerfil.mutate()}
            disabled={!novoNome.trim() || criarPerfil.isPending}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
          >
            Criar
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-5">
        {/* Lista de perfis */}
        <div className="space-y-2">
          {perfis.map(p => (
            <button
              key={p.id}
              onClick={() => setPerfilSelId(p.id)}
              className={`w-full text-left p-3 rounded-xl border-2 transition-all
                ${perfilSelId === p.id ? 'border-red-500 bg-red-50' : 'border-gray-200 bg-white hover:border-gray-300'}`}
            >
              <p className="font-medium text-gray-900 flex items-center gap-1.5">
                {p.nome}
                {p.protegido && <Lock size={11} className="text-gray-400" />}
              </p>
              {p.descricao && <p className="text-xs text-gray-500 mt-0.5">{p.descricao}</p>}
            </button>
          ))}
        </div>

        {/* Matriz de permissões */}
        <div className="lg:col-span-3">
          {!perfilSel && (
            <p className="text-gray-400 text-sm">Selecione um perfil para editar suas permissões.</p>
          )}

          {perfilSel?.superAdmin && (
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-800">
              O perfil SUPER_ADMIN tem acesso total e irrestrito a todos os módulos, por definição.
              Suas permissões não podem ser alteradas.
            </div>
          )}

          {perfilSel && !perfilSel.superAdmin && (
            <div className="bg-white rounded-xl shadow overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Módulo</th>
                    {ACOES.map(a => (
                      <th key={a.campo} className="px-3 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-20">
                        {a.rotulo}
                      </th>
                    ))}
                    <th className="px-3 py-3 w-24" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {modulos.map(m => (
                    <tr key={m.nome} className="hover:bg-gray-50">
                      <td className="px-4 py-2.5 font-medium text-gray-800">{m.rotulo}</td>
                      {ACOES.map(a => (
                        <td key={a.campo} className="px-3 py-2.5 text-center">
                          <input
                            type="checkbox"
                            checked={matriz[m.nome]?.[a.campo] ?? false}
                            onChange={() => alternar(m.nome, a.campo)}
                            className="w-4 h-4 rounded border-gray-300 text-red-600 focus:ring-red-500 cursor-pointer"
                          />
                        </td>
                      ))}
                      <td className="px-3 py-2.5 text-right">
                        <button
                          onClick={() => marcarTodos(m.nome, !(matriz[m.nome]?.podeVer && matriz[m.nome]?.podeCriar
                            && matriz[m.nome]?.podeEditar && matriz[m.nome]?.podeExcluir))}
                          className="text-xs text-gray-400 hover:text-red-600"
                        >
                          alternar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="flex items-center justify-between px-4 py-3 bg-gray-50 border-t">
                <button
                  onClick={() => {
                    if (confirm(`Excluir o perfil "${perfilSel.nome}"? Essa ação não pode ser desfeita.`)) {
                      excluirPerfil.mutate(perfilSel.id)
                    }
                  }}
                  disabled={perfilSel.protegido}
                  className="flex items-center gap-1.5 px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <Trash2 size={14} /> Excluir perfil
                </button>
                <button
                  onClick={() => salvarPermissoes.mutate()}
                  disabled={salvarPermissoes.isPending}
                  className="flex items-center gap-1.5 px-5 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
                >
                  <Save size={15} />
                  {salvarPermissoes.isPending ? 'Salvando...' : 'Salvar Permissões'}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}