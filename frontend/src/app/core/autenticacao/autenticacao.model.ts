export interface AutenticacaoRequest {
  readonly email: string;
  readonly senha: string;
}

export interface UsuarioAutenticado {
  readonly id: number;
  readonly nome: string;
  readonly email: string;
  readonly perfil: 'ALUNO' | 'ADMINISTRADOR';
  readonly xpTotal: number;
  readonly nivel: number;
  readonly diasOfensiva: number;
  readonly token: string;
}
