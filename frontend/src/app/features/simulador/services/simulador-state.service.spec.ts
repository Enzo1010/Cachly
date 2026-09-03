import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SimuladorStateService } from './simulador-state.service';
import { SimuladorCacheService } from './simulador-cache.service';
import { SessaoService } from '../../../core/autenticacao/sessao.service';
import { DesafioCache, ResultadoDesafio } from '../models/desafio.model';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('SimuladorStateService', () => {
  let service: SimuladorStateService;
  let mockSimuladorService: Partial<SimuladorCacheService>;
  let mockSessaoService: Partial<SessaoService>;

  const mockDesafio: DesafioCache = {
    id: 'cold-miss',
    titulo: 'Miss Compulsorio',
    descricao: 'Descricao teste',
    dificuldade: 'FACIL',
    xpRecompensa: 30,
    configuracao: {
      tamanhoCacheBytes: 64,
      tamanhoBlocoBytes: 16,
      numeroVias: null,
      mapeamento: 'DIRETO',
      substituicao: 'LRU',
      enderecos: [0, 16]
    },
    pergunta: 'Quantos misses?',
    opcoes: [{ id: 'A', texto: 'Opcao A' }],
    dica: 'Dica'
  };

  const mockResultado: ResultadoDesafio = {
    correto: true,
    opcaoCorretaId: 'A',
    explicacao: 'Parabens',
    xpGanho: 30,
    simulacao: {
      totalAcessos: 2,
      totalHits: 0,
      totalMisses: 2,
      taxaHit: 0,
      taxaMiss: 100,
      bitsTag: 26,
      bitsIndice: 2,
      bitsOffset: 4,
      totalLinhas: 4,
      totalConjuntos: 4,
      passos: [
        {
          passoNumero: 1,
          endereco: 0,
          tag: 0,
          indice: 0,
          offset: 0,
          hit: false,
          blocoSubstituido: null,
          deltaLinha: { indiceLinha: 0, conjuntoIndex: 0, valida: true, tag: 0 },
          explicacao: 'Miss passo 1'
        }
      ]
    },
    nivelAtual: 2,
    nomeNivel: 'Dev Junior',
    xpTotalAtual: 130
  };

  beforeEach(() => {
    mockSimuladorService = {
      executarSimulacao: vi.fn(),
      listarDesafios: vi.fn().mockReturnValue(of([mockDesafio])),
      verificarDesafio: vi.fn().mockReturnValue(of(mockResultado))
    };

    mockSessaoService = {
      atualizarAposResposta: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        SimuladorStateService,
        { provide: SimuladorCacheService, useValue: mockSimuladorService },
        { provide: SessaoService, useValue: mockSessaoService }
      ]
    });

    service = TestBed.inject(SimuladorStateService);
  });

  it('deve inicializar no modo livre e com listas vazias', () => {
    expect(service.modo()).toBe('livre');
    expect(service.desafios()).toEqual([]);
    expect(service.desafioSelecionado()).toBeNull();
  });

  it('deve alternar para modo desafio e carregar os desafios', () => {
    service.setModo('desafio');

    expect(service.modo()).toBe('desafio');
    expect(mockSimuladorService.listarDesafios).toHaveBeenCalled();
    expect(service.desafios().length).toBe(1);
    expect(service.desafioSelecionado()?.id).toBe('cold-miss');
  });

  it('deve selecionar desafio e resetar opcao e resultado', () => {
    service.selecionarOpcao('B');
    service.selecionarDesafio(mockDesafio);

    expect(service.desafioSelecionado()?.id).toBe('cold-miss');
    expect(service.opcaoSelecionada()).toBeNull();
    expect(service.resultadoDesafio()).toBeNull();
  });

  it('deve verificar desafio com sucesso e atualizar sessao', () => {
    service.selecionarDesafio(mockDesafio);
    service.selecionarOpcao('A');
    service.verificarDesafio();

    expect(mockSimuladorService.verificarDesafio).toHaveBeenCalledWith('cold-miss', { opcaoSelecionadaId: 'A' });
    expect(service.resultadoDesafio()?.correto).toBe(true);
    expect(service.simulacao()?.totalMisses).toBe(2);
    expect(mockSessaoService.atualizarAposResposta).toHaveBeenCalledWith(130, 2, 'Dev Junior');
  });
});
