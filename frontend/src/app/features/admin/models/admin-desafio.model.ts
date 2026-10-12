import { SimulacaoRequest } from '../../simulador/models/simulador.model';

export interface OpcaoDesafio {
  id: string;
  texto: string;
}

export interface DesafioSimuladorRequest {
  id?: string;
  titulo: string;
  descricao: string;
  dificuldade: 'FACIL' | 'MEDIO' | 'DIFICIL';
  xpRecompensa: number;
  configuracao: SimulacaoRequest;
  pergunta: string;
  opcoes: OpcaoDesafio[];
  opcaoCorretaId: string;
  dica: string;
  explicacao: string;
}

export interface DesafioCacheResponse {
  id: string;
  titulo: string;
  descricao: string;
  dificuldade: 'FACIL' | 'MEDIO' | 'DIFICIL';
  xpRecompensa: number;
  configuracao: SimulacaoRequest;
  pergunta: string;
  opcoes: OpcaoDesafio[];
  opcaoCorretaId: string;
  dica: string;
  explicacao: string;
}
