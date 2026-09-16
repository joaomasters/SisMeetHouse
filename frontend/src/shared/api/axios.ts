import axios from 'axios'
import toast from 'react-hot-toast'
import { getToken, removeToken } from '../auth'

export const api = axios.create({
  baseURL: '/api',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
      return Promise.reject(error)
    }
    const data = error.response?.data
    let msg = 'Erro desconhecido'
    if (typeof data === 'string' && data.trim()) {
      msg = data
    } else if (data && typeof data === 'object') {
      const d = data as Record<string, unknown>
      if (typeof d.message === 'string' && d.message.trim()) {
        msg = d.message
      } else if (typeof d.detail === 'string' && d.detail.trim()) {
        msg = d.detail
      } else if (typeof d.error === 'string' && d.error.trim()) {
        msg = d.error
      } else if (Array.isArray(d.errors) && d.errors.length > 0) {
        const first = d.errors[0] as Record<string, unknown>
        msg = String(first.defaultMessage ?? first.message ?? 'Erro de validação')
      } else if (Array.isArray(d.fieldErrors) && d.fieldErrors.length > 0) {
        const first = d.fieldErrors[0] as Record<string, unknown>
        msg = String(first.defaultMessage ?? first.message ?? 'Campo inválido')
      }
    } else if (error.message) {
      msg = error.message
    }
    toast.error(msg)
    return Promise.reject(error)
  }
)
