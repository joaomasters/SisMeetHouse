import { NavLink, useNavigate } from 'react-router-dom'
import {
  ShoppingCart, Package, Scissors, DollarSign,
  CreditCard, BarChart2, Scale, AlertTriangle,
  ClipboardList, TrendingDown, ArrowDownCircle, BarChart, LogOut,
  Truck, FileText, Users, ShieldCheck, Home, Contact
} from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import clsx from 'clsx'
import { api } from '../api/axios'
import { removeSessao } from '../auth'
import { usePermissao } from '../hooks/usePermissao'

const nav = [
  { label: 'Início', href: '/inicio', icon: Home, sempreVisivel: true },
  { label: 'PDV / Caixa',          href: '/pdv',              icon: ShoppingCart,   external: true, modulo: 'PDV' },
  { label: 'Sangria / Suprimento', href: '/pdv/sangria',      icon: ArrowDownCircle, modulo: 'SANGRIA' },

  { separator: 'Estoque' },
  { label: 'Produtos',          href: '/estoque/produtos',       icon: Package,        modulo: 'PRODUTOS' },
  { label: 'Recebimento',       href: '/estoque/recebimento',    icon: Truck,          modulo: 'RECEBIMENTO' },
  { label: 'Fichas Desossa',    href: '/estoque/fichas-desossa', icon: ClipboardList,  modulo: 'FICHAS_DESOSSA' },
  { label: 'Rateio de Desossa', href: '/estoque/desossa',        icon: Scissors,       modulo: 'RATEIO_DESOSSA' },
  { label: 'Inventário',        href: '/estoque/inventario',     icon: ClipboardList,  modulo: 'INVENTARIO' },
  { label: 'Perdas',            href: '/estoque/perdas',         icon: AlertTriangle,  modulo: 'PERDAS' },

  { separator: 'Fiscal' },
  { label: 'NF de Saída', href: '/fiscal/notas', icon: FileText, modulo: 'NF_SAIDA' },

  { separator: 'Financeiro' },
  { label: 'Clientes',         href: '/financeiro/clientes',       icon: Contact,      modulo: 'CLIENTES' },
  { label: 'Faturamento',      href: '/financeiro/faturamento',    icon: DollarSign,  modulo: 'FATURAMENTO' },
  { label: 'Contas a Receber', href: '/financeiro/contas-receber', icon: CreditCard,  modulo: 'CONTAS_RECEBER' },
  { label: 'Contas a Pagar',   href: '/financeiro/contas-pagar',   icon: TrendingDown, modulo: 'CONTAS_PAGAR' },
  { label: 'DRE',              href: '/financeiro/dre',            icon: BarChart2,   modulo: 'DRE' },
  { label: 'Relatórios',       href: '/financeiro/relatorios',     icon: BarChart,    modulo: 'RELATORIOS' },

  { separator: 'Balança' },
  { label: 'Carga Balança', href: '/balanca', icon: Scale, modulo: 'CARGA_BALANCA' },

  { separator: 'Administração' },
  { label: 'Usuários', href: '/acesso/usuarios', icon: Users,       modulo: 'USUARIOS' },
  { label: 'Perfis',   href: '/acesso/perfis',   icon: ShieldCheck, modulo: 'PERFIS' },
]

export default function Sidebar() {
  const navigate = useNavigate()
  const { podeVer, isSuperAdmin } = usePermissao()

  const podeVerBalanca = isSuperAdmin || podeVer('CARGA_BALANCA')

  // Badge de pendências de preço na balança — só busca se o usuário
  // realmente enxerga o módulo, pra não gerar chamada desnecessária.
  const { data: pendentesBalanca } = useQuery<{ total: number }>({
    queryKey: ['balanca-pendentes-count'],
    queryFn: () => api.get('/balanca/pendentes/count').then(r => r.data),
    enabled: podeVerBalanca,
    refetchInterval: 60_000,
  })
  const qtdPendentesBalanca = pendentesBalanca?.total ?? 0

  const logout = () => {
    removeSessao()
    navigate('/login')
  }

  // Filtra os itens pelo que o perfil pode VER, e depois remove separadores
  // que ficaram sem nenhum item visível embaixo (evita título "solto").
  const itensVisiveis = nav.filter(item =>
    'separator' in item || item.sempreVisivel || isSuperAdmin || podeVer(item.modulo!)
  )
  const navFiltrado = itensVisiveis.filter((item, i) => {
    if (!('separator' in item)) return true
    const proximo = itensVisiveis[i + 1]
    return proximo && !('separator' in proximo)
  })

  return (
    <aside className="w-60 bg-gray-900 text-white flex flex-col min-h-screen">
      <div className="px-5 py-5 border-b border-gray-700">
        <p className="text-lg font-bold text-red-400 tracking-wide">🥩 AçougueERP</p>
        <p className="text-xs text-gray-400 mt-0.5">Sistema de Gestão</p>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {navFiltrado.map((item, i) => {
          if ('separator' in item) {
            return (
              <p key={i} className="px-2 pt-4 pb-1 text-xs font-semibold text-gray-500 uppercase tracking-widest">
                {item.separator}
              </p>
            )
          }
          const Icon = item.icon!
          if (item.external) {
            return (
              <a
                key={item.href}
                href={item.href}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-300 hover:bg-red-700 hover:text-white transition-colors"
              >
                <Icon size={16} />
                {item.label}
              </a>
            )
          }
          return (
            <NavLink
              key={item.href}
              to={item.href!}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors',
                  isActive
                    ? 'bg-red-700 text-white font-medium'
                    : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                )
              }
            >
              <Icon size={16} />
              <span className="flex-1">{item.label}</span>
              {item.href === '/balanca' && qtdPendentesBalanca > 0 && (
                <span className="bg-amber-500 text-gray-900 text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                  {qtdPendentesBalanca > 99 ? '99+' : qtdPendentesBalanca}
                </span>
              )}
            </NavLink>
          )
        })}
      </nav>

      <div className="px-3 py-3 border-t border-gray-700">
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-400 hover:bg-gray-800 hover:text-white transition-colors w-full"
        >
          <LogOut size={16} />
          Sair
        </button>
        <p className="px-3 pt-2 text-xs text-gray-600">v2.0.0 — Java 17 + React 18</p>
      </div>
    </aside>
  )
}