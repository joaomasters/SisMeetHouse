import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import NotificacaoEstoque from './NotificacaoEstoque'

export default function Layout() {
  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-end px-5 shrink-0">
          <NotificacaoEstoque />
        </header>
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}