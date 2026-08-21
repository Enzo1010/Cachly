export type StatusConquista = 'desbloqueada' | 'em-progresso';

export type TemaConquista = 'ambar' | 'neutro' | 'azul';

export interface Conquista {
  readonly id: string;
  readonly titulo: string;
  readonly descricao: string;
  readonly icone: string;
  readonly tema: TemaConquista;
  readonly status: StatusConquista;
  readonly progresso?: number;
}
