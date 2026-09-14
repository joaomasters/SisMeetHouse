export interface PerfilPermissaoItem {
  id?: number
  modulo: string
  podeVer: boolean
  podeCriar: boolean
  podeEditar: boolean
  podeExcluir: boolean
}

export interface Perfil {
  id: number
  nome: string
  descricao?: string
  protegido: boolean
  ativo: boolean
  superAdmin: boolean
  permissoes: PerfilPermissaoItem[]
  createdAt?: string
}

export interface Usuario {
  id: number
  nome: string
  login: string
  perfil: Perfil
  ativo: boolean
  createdAt?: string
}

export interface ModuloInfo {
  nome: string
  rotulo: string
}