export interface RankingAluno {
  readonly posicao: number;
  readonly nome: string;
  readonly nivel: number;
  readonly xpTotal: number;
  readonly diasOfensiva: number;
}

export interface PaginaRanking {
  readonly content: readonly RankingAluno[];
}
