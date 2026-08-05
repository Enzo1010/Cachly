export interface CadastroAlunoRequest {
  readonly nome: string;
  readonly email: string;
  readonly senha: string;
}

export interface AlunoCadastrado {
  readonly id: number;
  readonly nome: string;
  readonly email: string;
}
