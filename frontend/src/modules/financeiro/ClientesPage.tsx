import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Contact, Plus, Search, Pencil, Ban, RotateCcw } from 'lucide-react'
import { api } from '@/shared/api/axios'
import toast from 'react-hot-toast'
import type { Cliente } from '@/types/venda'

const brl = (v?: number) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const tipoClienteLabel: Record<Cliente['tipoCliente'], string> = {
  VAREJO: 'Varejo',
  ATACADO: 'Atacado',
  RESTAURANTE: 'Restaurante',
  CONVENIADO: 'Conveniado',
}

const tipoClienteCor: Record<Cliente['tipoCliente'], string> = {
  VAREJO: 'bg-gray-100 text-gray-600',
  ATACADO: 'bg-blue-100 text-blue-700',
  RESTAURANTE: 'bg-orange-100 text-orange-700',
  CONVENIADO: 'bg-purple-100 text-purple-700',
}

type FormState = {
  id?: number
  nome: string
  cpfCnpj: string
  tipoPessoa: 'PF' | 'PJ'
  telefone: string
  email: string
  endereco: string
  tipoCliente: Cliente['tipoCliente']
  limiteCredito: string
}

const formVazio: FormState = {
  nome: '', cpfCnpj: '', tipoPessoa: 'PF', telefone: '', email: '',
  endereco: '', tipoCliente: 'VAREJO', limiteCredito: '0',
}

export default function ClientesPage() {
  const qc = useQueryClient()
  const [busca, setBusca] = useState('')
  const [form, setForm] = useState<FormState | null>(null)
  const [mostrarInativos, setMostrarInativos] = useState(false)

  const { data: clientes = [], isLoading } = useQuery<Cliente[]>({
    queryKey: ['clientes', busca],
    queryFn: () => api.get('/clientes', { params: { nome: busca || undefined } }).then(r => r.data),
  })

  const visiveis = clientes.filter(c => mostrarInativos || c.ativo)

  const salvar = useMutation({
    mutationFn: (f: FormState) => {
      const payload = {
        nome: f.nome.trim(),
        cpfCnpj: f.cpfCnpj.trim() || null,
        tipoPessoa: f.tipoPessoa,
        telefone: f.telefone.trim() || null,
        email: f.email.trim() || null,
        endereco: f.endereco.trim() || null,
        tipoCliente: f.tipoCliente,
        limiteCredito: parseFloat(f.limiteCredito.replace(',', '.')) || 0,
      }
      return f.id
        ? api.put(`/clientes/${f.id}`, payload)
        : api.post('/clientes', payload)
    },
    onSuccess: () => {
      toast.success(form?.id ? 'Cliente atualizado!' : 'Cliente cadastrado!')
      qc.invalidateQueries({ queryKey: ['clientes'] })
      setForm(null)
    },
  })

  const inativar = useMutation({
    mutationFn: (id: number) => api.post(`/clientes/${id}/inativar`),
    onSuccess: () => {
      toast.success('Cliente inativado.')
      qc.invalidateQueries({ queryKey: ['clientes'] })
    },
  })

  const reativar = useMutation({
    mutationFn: (id: number) => api.post(`/clientes/${id}/reativar`),
    onSuccess: () => {
      toast.success('Cliente reativado.')
      qc.invalidateQueries({ queryKey: ['clientes'] })
    },
  })

  const abrirEdicao = (c: Cliente) => setForm({
    id: c.id,
    nome: c.nome,
    cpfCnpj: c.cpfCnpj ?? '',
    tipoPessoa: c.tipoPessoa,
    telefone: c.telefone ?? '',
    email: c.email ?? '',
    endereco: c.endereco ?? '',
    tipoCliente: c.tipoCliente,
    limiteCredito: String(c.limiteCredito ?? 0),
  })

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <Contact className="text-red-600" /> Clientes
          </h1>
          <p className="text-gray-500 text-sm">Cadastro de clientes atacado, restaurantes e conveniados</p>
        </div>
        <button
          onClick={() => setForm({ ...formVazio })}
          className="flex items-center gap-2 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium"
        >
          <Plus size={18} /> Novo Cliente
        </button>
      </div>

      {/* Filtros */}
      <div className="flex items-center gap-3 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text" value={busca} onChange={e => setBusca(e.target.value)}
            placeholder="Buscar por nome..."
            className="w-full border rounded-lg pl-9 pr-3 py-2 text-sm"
          />
        </div>
        <label className="flex items-center gap-2 text-sm text-gray-600">
          <input type="checkbox" checked={mostrarInativos} onChange={e => setMostrarInativos(e.target.checked)} />
          Mostrar inativos
        </label>
      </div>

      {/* Lista */}
      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Nome</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tipo</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Contato</th>
              <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Limite Crédito</th>
              <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Saldo Fiado</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {visiveis.map(c => (
              <tr key={c.id} className={`hover:bg-gray-50 ${!c.ativo ? 'opacity-50' : ''}`}>
                <td className="px-4 py-3 font-medium">
                  {c.nome}
                  {!c.ativo && <span className="ml-2 text-xs text-gray-400">(inativo)</span>}
                </td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${tipoClienteCor[c.tipoCliente]}`}>
                    {tipoClienteLabel[c.tipoCliente]}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500 text-xs">
                  {c.telefone || c.email || '—'}
                </td>
                <td className="px-4 py-3 text-right tabular-nums">{brl(c.limiteCredito)}</td>
                <td className={`px-4 py-3 text-right tabular-nums font-medium ${(c.saldoFiadoAtual ?? 0) > 0 ? 'text-red-600' : 'text-gray-400'}`}>
                  {brl(c.saldoFiadoAtual)}
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-end gap-2">
                    <button onClick={() => abrirEdicao(c)} title="Editar"
                      className="text-gray-400 hover:text-blue-600">
                      <Pencil size={15} />
                    </button>
                    {c.ativo ? (
                      <button onClick={() => inativar.mutate(c.id)} title="Inativar"
                        className="text-gray-400 hover:text-red-600">
                        <Ban size={15} />
                      </button>
                    ) : (
                      <button onClick={() => reativar.mutate(c.id)} title="Reativar"
                        className="text-gray-400 hover:text-emerald-600">
                        <RotateCcw size={15} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!isLoading && visiveis.length === 0 && (
          <p className="text-center text-gray-400 py-10 text-sm">Nenhum cliente encontrado.</p>
        )}
      </div>

      {/* Modal de cadastro/edição */}
      {form && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <h2 className="font-bold text-gray-900">{form.id ? 'Editar Cliente' : 'Novo Cliente'}</h2>

            <div>
              <label className="text-xs font-medium text-gray-600 block mb-1">Nome *</label>
              <input
                type="text" value={form.nome} autoFocus
                onChange={e => setForm({ ...form, nome: e.target.value })}
                className="w-full border rounded-lg px-3 py-2 text-sm"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Tipo de Pessoa</label>
                <select value={form.tipoPessoa} onChange={e => setForm({ ...form, tipoPessoa: e.target.value as 'PF' | 'PJ' })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="PF">Pessoa Física</option>
                  <option value="PJ">Pessoa Jurídica</option>
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">CPF/CNPJ</label>
                <input type="text" value={form.cpfCnpj} onChange={e => setForm({ ...form, cpfCnpj: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Telefone</label>
                <input type="text" value={form.telefone} onChange={e => setForm({ ...form, telefone: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">E-mail</label>
                <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-gray-600 block mb-1">Endereço</label>
              <textarea value={form.endereco} onChange={e => setForm({ ...form, endereco: e.target.value })}
                rows={2} className="w-full border rounded-lg px-3 py-2 text-sm" />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Tipo de Cliente</label>
                <select value={form.tipoCliente} onChange={e => setForm({ ...form, tipoCliente: e.target.value as Cliente['tipoCliente'] })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="VAREJO">Varejo</option>
                  <option value="ATACADO">Atacado</option>
                  <option value="RESTAURANTE">Restaurante</option>
                  <option value="CONVENIADO">Conveniado</option>
                </select>
                <p className="text-[11px] text-gray-400 mt-1">
                  Só clientes Atacado/Restaurante/Conveniado aparecem pra Faturamento e Fiado.
                </p>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-600 block mb-1">Limite de Crédito (R$)</label>
                <input type="number" step="0.01" value={form.limiteCredito}
                  onChange={e => setForm({ ...form, limiteCredito: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" />
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button onClick={() => setForm(null)} className="flex-1 py-2.5 border rounded-lg text-sm hover:bg-gray-50">
                Cancelar
              </button>
              <button
                onClick={() => salvar.mutate(form)}
                disabled={!form.nome.trim() || salvar.isPending}
                className="flex-1 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium disabled:opacity-60"
              >
                {salvar.isPending ? 'Salvando...' : 'Salvar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}