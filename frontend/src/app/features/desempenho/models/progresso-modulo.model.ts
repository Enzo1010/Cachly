export type StatusTopico = 'concluido' | 'atual' | 'bloqueado';

export interface TopicoModulo {
  readonly nome: string;
  readonly status: StatusTopico;
}

export interface ProgressoModulo {
  readonly nomeCategoria: string;
  readonly progresso: number;
  readonly topicos: readonly TopicoModulo[];
}
