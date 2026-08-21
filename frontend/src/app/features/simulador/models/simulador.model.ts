export type TipoMapeamento = 'DIRETO' | 'TOTALMENTE_ASSOCIATIVO' | 'CONJUNTO_ASSOCIATIVO';

export type PoliticaSubstituicao = 'LRU' | 'FIFO';

export interface SimulacaoRequest {
  tamanhoCacheBytes: number;
  tamanhoBlocoBytes: number;
  numeroVias: number | null;
  mapeamento: TipoMapeamento;
  substituicao: PoliticaSubstituicao | null;
  enderecos: number[];
}

export interface EstadoLinhaCacheResponse {
  indiceLinha: number;
  conjuntoIndex: number | null;
  valida: boolean;
  tag: number | null;
}

export interface PassoSimulacaoResponse {
  passoNumero: number;
  endereco: number;
  tag: number;
  indice: number | null;
  offset: number;
  hit: boolean;
  blocoSubstituido: number | null;
  deltaLinha: EstadoLinhaCacheResponse | null;
}

export interface SimulacaoResponse {
  bitsOffset: number;
  bitsIndice: number;
  bitsTag: number;
  totalLinhas: number;
  totalConjuntos: number;
  totalAcessos: number;
  totalHits: number;
  totalMisses: number;
  taxaHit: number;
  taxaMiss: number;
  passos: PassoSimulacaoResponse[];
}
