import axios from 'axios'
import toast from 'react-hot-toast'

export const api = axios.create({
  baseURL: '/api',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const msg =
      error.response?.data?.message ??
      error.response?.data ??
      error.message ??
      'Erro desconhecido'
    toast.error(String(msg))
    return Promise.reject(error)
  }
)
