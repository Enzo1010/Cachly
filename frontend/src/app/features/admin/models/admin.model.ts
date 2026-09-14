export type DificuldadeQuestao = 'FACIL' | 'MEDIO' | 'DIFICIL';

export interface AlternativaAdminRequest {
  id?: number | null;
  texto: string;
  correta: boolean;
  ordem: number;
}

export interface AlternativaAdminResponse {
  id: number;
  questaoId: number;
  texto: string;
  correta: boolean;
  ordem: number;
  ativa: boolean;
  criadoEm?: string;
  atualizadoEm?: string;
}

export interface QuestaoAdminRequest {
  categoriaId: number;
  enunciado: string;
  explicacao: string;
  dificuldade: DificuldadeQuestao;
  xpBase: number;
  alternativas: AlternativaAdminRequest[];
}

export interface QuestaoAdminResponse {
  id: number;
  categoriaId: number;
  categoriaNome: string;
  enunciado: string;
  explicacao: string;
  dificuldade: DificuldadeQuestao;
  xpBase: number;
  ativa: boolean;
  alternativas: AlternativaAdminResponse[];
  criadoEm?: string;
  atualizadoEm?: string;
}

export interface CategoriaAdminRequest {
  nome: string;
  descricao?: string | null;
}

export interface CategoriaAdminResponse {
  id: number;
  nome: string;
  descricao?: string | null;
  ativa: boolean;
  criadoEm?: string;
  atualizadoEm?: string;
}

export interface MetricasAdmin {
  totalQuestoes: number;
  questoesAtivas: number;
  questoesInativas: number;
  totalCategorias: number;
  categoriasAtivas: number;
  categoriasInativas: number;
  questoesFaceis: number;
  questoesMedias: number;
  questoesDificeis: number;
}
