import { Injectable } from '@angular/core';

import { DesempenhoCategoriaResponse } from '../models/desempenho.model';
import { ProgressoModulo, StatusTopico, TopicoModulo } from '../models/progresso-modulo.model';

// TODO: o backend ainda não tem o conceito de "tópicos" dentro de uma categoria
// (só existe Categoria: id, nome, descrição). Os nomes de tópicos abaixo são fixos
// até que exista um endpoint real para isso; o progresso (%) e a categoria em si,
// porém, já vêm de dados reais (estatisticasPorCategoria de /api/alunos/me/desempenho).
const TOPICOS_POR_CATEGORIA: Readonly<Record<string, readonly string[]>> = {
  'sistemas numéricos': ['Binário e Decimal', 'Hexadecimal', 'Complemento de Dois', 'Ponto Flutuante'],
  'portas lógicas': ['AND, OR e NOT', 'NAND e NOR', 'Tabelas-Verdade', 'Circuitos Combinacionais'],
  'álgebra booleana': [
    'Axiomas e Propriedades',
    'Simplificação de Expressões',
    'Mapas de Karnaugh',
    'Formas Canônicas',
  ],
  memória: [
    'Hierarquia de Memória',
    'Memória Principal (RAM)',
    'Memória Cache (Mapeamentos)',
    'Memória Virtual',
  ],
  cpu: ['Componentes da CPU', 'Ciclo de Busca e Execução', 'Registradores', 'Unidade de Controle'],
  pipeline: [
    'Estágios do Pipeline',
    'Hazards Estruturais',
    'Hazards de Dados',
    'Hazards de Controle',
  ],
};

const TOPICOS_GENERICOS: readonly string[] = [
  'Fundamentos do módulo',
  'Prática guiada',
  'Exercícios avançados',
  'Revisão final',
];

@Injectable({ providedIn: 'root' })
export class ProgressoModuloService {
  obterProgresso(categoria: DesempenhoCategoriaResponse): ProgressoModulo {
    const nomesTopicos =
      TOPICOS_POR_CATEGORIA[categoria.categoriaNome.trim().toLowerCase()] ?? TOPICOS_GENERICOS;

    return {
      nomeCategoria: categoria.categoriaNome,
      progresso: categoria.taxaAcerto,
      topicos: this.calcularStatusTopicos(nomesTopicos, categoria.taxaAcerto),
    };
  }

  private calcularStatusTopicos(
    nomes: readonly string[],
    progresso: number,
  ): readonly TopicoModulo[] {
    const quantidadeConcluida = Math.min(
      nomes.length,
      Math.floor((progresso / 100) * nomes.length),
    );

    return nomes.map((nome, indice) => ({
      nome,
      status: this.statusPorIndice(indice, quantidadeConcluida, nomes.length),
    }));
  }

  private statusPorIndice(
    indice: number,
    quantidadeConcluida: number,
    total: number,
  ): StatusTopico {
    if (indice < quantidadeConcluida) {
      return 'concluido';
    }

    if (indice === quantidadeConcluida && quantidadeConcluida < total) {
      return 'atual';
    }

    return 'bloqueado';
  }
}
