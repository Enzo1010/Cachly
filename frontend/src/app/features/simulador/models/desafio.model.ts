import { SimulacaoRequest, SimulacaoResponse } from './simulador.model';

export interface OpcaoDesafio {
  readonly id: string;
  readonly texto: string;
}

export interface DesafioCache {
  readonly id: string;
  readonly titulo: string;
  readonly descricao: string;
  readonly dificuldade: 'FACIL' | 'MEDIO' | 'DIFICIL';
  readonly xpRecompensa: number;
  readonly configuracao: SimulacaoRequest;
  readonly pergunta: string;
  readonly opcoes: readonly OpcaoDesafio[];
  readonly dica: string;
}

export interface VerificarDesafioRequest {
  readonly opcaoSelecionadaId: string;
}

export interface ResultadoDesafio {
  readonly correto: boolean;
  readonly opcaoCorretaId: string;
  readonly explicacao: string;
  readonly xpGanho: number;
  readonly simulacao: SimulacaoResponse;
  readonly nivelAtual: number;
  readonly nomeNivel: string;
  readonly xpTotalAtual: number;
}
