import { NavLink } from 'react-router-dom'
import {
  ShoppingCart, Package, Scissors, DollarSign,
  CreditCard, BarChart2, Scale, AlertTriangle,
  ClipboardList, TrendingDown, ArrowDownCircle, BarChart
} from 'lucide-react'
import clsx from 'clsx'

const nav = [
  {
    label: 'PDV / Caixa',
    href: '/pdv',
    icon: ShoppingCart,
    external: true,
  },
  { label: 'Sangria / Suprimento', href: '/pdv/sangria', icon: ArrowDownCircle },

  { separator: 'Estoque' },
  { label: 'Produtos',    href: '/estoque/produtos',   icon: Package },
  { label: 'Desossa',     href: '/estoque/desossa',    icon: Scissors },
  { label: 'Inventário',  href: '/estoque/inventario', icon: ClipboardList },
  { label: 'Perdas',      href: '/estoque/perdas',     icon: AlertTriangle },

  { separator: 'Financeiro' },
  { label: 'Faturamento',      href: '/financeiro/faturamento',    icon: DollarSign },
  { label: 'Contas a Receber', href: '/financeiro/contas-receber', icon: CreditCard },
  { label: 'Contas a Pagar',   href: '/financeiro/contas-pagar',   icon: TrendingDown },
  { label: 'DRE',              href: '/financeiro/dre',            icon: BarChart2 },
  { label: 'Relatórios',       href: '/financeiro/relatorios',     icon: BarChart },

  { separator: 'Balança' },
  { label: 'Carga Balança', href: '/balanca', icon: Scale },
]

export default function Sidebar() {
  return (
    <aside className="w-60 bg-gray-900 text-white flex flex-col min-h-screen">
      <div className="px-5 py-5 border-b border-gray-700">
        <p className="text-lg font-bold text-red-400 tracking-wide">🥩 AçougueERP</p>
        <p className="text-xs text-gray-400 mt-0.5">Sistema de Gestão</p>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {nav.map((item, i) => {
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
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm
                           text-gray-300 hover:bg-red-700 hover:text-white transition-colors"
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
              {item.label}
            </NavLink>
          )
        })}
      </nav>

      <div className="px-5 py-4 border-t border-gray-700 text-xs text-gray-500">
        v2.0.0 — Java 17 + React 18
      </div>
    </aside>
  )
}
