export interface CategoriaResponse {
  readonly id: number;
  readonly nome: string;
  readonly descricao: string;
  readonly ativa: boolean;
  readonly criadoEm?: string;
  readonly atualizadoEm?: string;
  readonly criadoPor?: number;
  readonly atualizadoPor?: number;
}

export type DificuldadeQuestao = 'FACIL' | 'MEDIA' | 'DIFICIL';

export interface AlternativaEstudoResponse {
  readonly id: number;
  readonly texto: string;
  readonly ordem: number;
}

export interface QuestaoEstudoResponse {
  readonly id: number;
  readonly enunciado: string;
  readonly dificuldade: DificuldadeQuestao;
  readonly xpBase: number;
  readonly alternativas: readonly AlternativaEstudoResponse[];
}

export interface RespostaRequest {
  readonly alternativaId: number;
}

export interface RespostaResponse {
  readonly tentativaId: number;
  readonly correta: boolean;
  readonly alternativaCorretaId: number;
  readonly explicacao: string;
  readonly xpConcedido: number;
  readonly nivelAtual: number;
  readonly nomeNivelAtual: string;
  readonly xpTotal: number;
}
