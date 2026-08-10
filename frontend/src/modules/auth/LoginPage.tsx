import { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { setToken } from '../../shared/auth'
import { Lock, User } from 'lucide-react'

export default function LoginPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await axios.post('/api/auth/login', { username, password })
      setToken(res.data.token)
      navigate('/')
    } catch {
      setError('Usuário ou senha incorretos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="text-5xl mb-3">🥩</div>
          <h1 className="text-2xl font-bold text-white">AçougueERP</h1>
          <p className="text-gray-400 text-sm mt-1">Sistema de Gestão</p>
        </div>

        <div className="bg-gray-800 rounded-2xl p-8 shadow-2xl border border-gray-700">
          <h2 className="text-white font-semibold text-base mb-6">Entrar no sistema</h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">Usuário</label>
              <div className="relative mt-1">
                <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  type="text"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  className="block w-full bg-gray-700 border border-gray-600 rounded-lg pl-9 pr-3 py-2.5 text-white placeholder-gray-500 text-sm focus:outline-none focus:border-red-500 transition"
                  placeholder="Digite seu usuário"
                  required
                  autoFocus
                />
              </div>
            </div>

            <div>
              <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">Senha</label>
              <div className="relative mt-1">
                <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  type="password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="block w-full bg-gray-700 border border-gray-600 rounded-lg pl-9 pr-3 py-2.5 text-white placeholder-gray-500 text-sm focus:outline-none focus:border-red-500 transition"
                  placeholder="••••••"
                  required
                />
              </div>
            </div>

            {error && (
              <div className="bg-red-900/30 border border-red-700 rounded-lg px-3 py-2">
                <p className="text-red-400 text-sm">{error}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-red-600 text-white rounded-lg font-medium text-sm hover:bg-red-700 disabled:opacity-50 transition mt-2"
            >
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>
        </div>

        <p className="text-center text-gray-600 text-xs mt-6">
          v2.0.0 — Java 17 + React 18
        </p>
      </div>
    </div>
  )
}
