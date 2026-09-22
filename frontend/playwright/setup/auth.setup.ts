import { request, type FullConfig } from '@playwright/test'
import * as dotenv from 'dotenv'
import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'

dotenv.config({ path: path.resolve(import.meta.dirname, '../../../.env') })

const authFile = path.resolve('playwright/.auth/user.json')
const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:5173'
const username = process.env.E2E_USERNAME
const password = process.env.E2E_PASSWORD

export default async function globalSetup(_config: FullConfig) {
  if (!username || !password) {
    throw new Error('Defina as variáveis E2E_USERNAME e E2E_PASSWORD antes de executar o Playwright.')
  }

  const apiContext = await request.newContext({ baseURL })
  const response = await apiContext.post('/api/auth/login', {
    data: {
      username,
      password,
    },
  })

  if (!response.ok()) {
    throw new Error(`Falha no login do Playwright: ${response.status()} ${await response.text()}`)
  }

  const sessao = await response.json()
  await mkdir(path.dirname(authFile), { recursive: true })
  await writeFile(authFile, JSON.stringify({
    cookies: [],
    origins: [{
      origin: new URL(baseURL).origin,
      localStorage: [{
        name: 'acougue_sessao',
        value: JSON.stringify(sessao),
      }],
    }],
  }, null, 2))

  await apiContext.dispose()
}