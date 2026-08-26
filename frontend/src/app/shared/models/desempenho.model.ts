export interface DesempenhoCategoriaResponse {
  readonly categoriaId: number;
  readonly categoriaNome: string;
  readonly totalTentativas: number;
  readonly acertos: number;
  readonly taxaAcerto: number;
}

export interface DesempenhoResponse {
  readonly totalTentativas: number;
  readonly acertos: number;
  readonly taxaAcerto: number;
  readonly estatisticasPorCategoria: readonly DesempenhoCategoriaResponse[];
}
