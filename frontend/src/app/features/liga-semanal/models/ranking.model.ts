/**
 * Espelha o RankingResponse (record) exposto por GET /api/ranking no backend.
 */
export interface RankingAluno {
  readonly posicao: number;
  readonly nome: string;
  readonly nivel: number;
  readonly xpTotal: number;
  readonly diasOfensiva: number;
}

/**
 * Espelha o formato de página do Spring Data (org.springframework.data.domain.Page)
 * retornado por GET /api/ranking. Só o campo "content" é usado nesta tela.
 */
export interface PaginaRanking {
  readonly content: readonly RankingAluno[];
}
