import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import App from './App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { error: Error | null }
> {
  state = { error: null }

  static getDerivedStateFromError(error: Error) {
    return { error }
  }

  render() {
    if (this.state.error) {
      const err = this.state.error as Error
      return (
        <div style={{
          minHeight: '100vh', display: 'flex', alignItems: 'center',
          justifyContent: 'center', background: '#111827', color: '#f9fafb',
          fontFamily: 'monospace', padding: 32,
        }}>
          <div style={{ maxWidth: 600, width: '100%' }}>
            <h1 style={{ color: '#ef4444', marginBottom: 16 }}>Erro no aplicativo</h1>
            <pre style={{
              background: '#1f2937', padding: 16, borderRadius: 8,
              fontSize: 13, overflowX: 'auto', marginBottom: 16,
            }}>
              {err.message}
              {'\n\n'}
              {err.stack}
            </pre>
            <button
              onClick={() => { localStorage.clear(); window.location.href = '/login' }}
              style={{
                background: '#dc2626', color: '#fff', border: 'none',
                padding: '10px 20px', borderRadius: 8, cursor: 'pointer',
                fontSize: 14, marginRight: 8,
              }}
            >
              Limpar sessão e voltar ao login
            </button>
            <button
              onClick={() => window.location.reload()}
              style={{
                background: '#374151', color: '#fff', border: 'none',
                padding: '10px 20px', borderRadius: 8, cursor: 'pointer',
                fontSize: 14,
              }}
            >
              Recarregar
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <App />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: { fontFamily: 'Inter, sans-serif', fontSize: '14px' },
          }}
        />
      </QueryClientProvider>
    </ErrorBoundary>
  </React.StrictMode>
)
