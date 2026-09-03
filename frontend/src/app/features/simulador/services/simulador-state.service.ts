import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, exhaustMap, tap, catchError, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SimulacaoRequest, SimulacaoResponse, PassoSimulacaoResponse } from '../models/simulador.model';
import { DesafioCache, ResultadoDesafio } from '../models/desafio.model';
import { SimuladorCacheService } from './simulador-cache.service';
import { SessaoService } from '../../../core/autenticacao/sessao.service';

interface BreakdownBinario {
  tag: string;
  indice: string;
  offset: string;
}

@Injectable()
export class SimuladorStateService {
  private readonly simuladorService = inject(SimuladorCacheService);
  private readonly sessao = inject(SessaoService, { optional: true });
  private readonly simularSubject = new Subject<SimulacaoRequest>();

  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  simulacao = signal<SimulacaoResponse | null>(null);
  passoAtualIndex = signal<number>(0);
  sidebarCollapsed = signal<boolean>(false);

  // Estado do Modo Desafios
  modo = signal<'livre' | 'desafio'>('livre');
  desafios = signal<readonly DesafioCache[]>([]);
  desafioSelecionado = signal<DesafioCache | null>(null);
  opcaoSelecionada = signal<string | null>(null);
  resultadoDesafio = signal<ResultadoDesafio | null>(null);
  carregandoDesafios = signal<boolean>(false);
  carregandoVerificacao = signal<boolean>(false);
  erroDesafio = signal<string | null>(null);

  constructor() {
    this.simularSubject.pipe(
      tap(() => {
        this.loading.set(true);
        this.error.set(null);
        this.simulacao.set(null);
        this.passoAtualIndex.set(0);
      }),
      exhaustMap((request) => 
        this.simuladorService.executarSimulacao(request).pipe(
          tap((response) => {
            this.simulacao.set(response);
            this.loading.set(false);
          }),
          catchError((err: HttpErrorResponse) => {
            this.loading.set(false);
            this.error.set(err.error?.mensagem || 'Erro ao comunicar com o servidor');
            return of(null);
          })
        )
      ),
      takeUntilDestroyed()
    ).subscribe();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }
  
  passoAtual = computed<PassoSimulacaoResponse | null>(() => {
    const sim = this.simulacao();
    const index = this.passoAtualIndex();
    if (sim && sim.passos && sim.passos.length > index) {
      return sim.passos[index];
    }
    return null;
  });

  estadoCacheAtual = computed(() => {
    const sim = this.simulacao();
    const currentIndex = this.passoAtualIndex();
    if (!sim || !sim.passos || sim.passos.length === 0) return [];

    const totalLinhas = sim.totalLinhas;
    const vias = sim.totalConjuntos > 0 ? totalLinhas / sim.totalConjuntos : totalLinhas;
    
    // Inicializa a cache vazia
    const cache = Array.from({ length: totalLinhas }, (_, i) => ({
      indiceLinha: i,
      conjuntoIndex: sim.totalConjuntos > 1 ? Math.floor(i / vias) : (sim.totalConjuntos === 1 ? 0 : null),
      valida: false,
      tag: null as number | null
    }));

    // Aplica os deltas até o passo atual com limite seguro
    const limite = Math.min(currentIndex, sim.passos.length - 1);
    for (let i = 0; i <= limite; i++) {
      const passo = sim.passos[i];
      if (passo && passo.deltaLinha && cache[passo.deltaLinha.indiceLinha]) {
        cache[passo.deltaLinha.indiceLinha] = { ...passo.deltaLinha };
      }
    }

    return cache;
  });
  
  isFirstStep = computed<boolean>(() => this.passoAtualIndex() === 0);
  isLastStep = computed<boolean>(() => {
    const sim = this.simulacao();
    return sim && sim.passos ? this.passoAtualIndex() === sim.passos.length - 1 : true;
  });

  totalBits = computed<number>(() => {
    const sim = this.simulacao();
    if (!sim) return 0;
    return sim.bitsTag + sim.bitsIndice + sim.bitsOffset;
  });

  binarioBreakdown = computed<BreakdownBinario | null>(() => {
    const sim = this.simulacao();
    const passo = this.passoAtual();
    const bits = this.totalBits();
    if (!sim || !passo || bits <= 0) return null;
    
    const binarioCompleto = (passo.endereco >>> 0).toString(2).padStart(bits, '0');
    
    const tag = binarioCompleto.substring(0, Math.max(0, sim.bitsTag));
    const indice = sim.bitsIndice > 0
      ? binarioCompleto.substring(sim.bitsTag, sim.bitsTag + sim.bitsIndice)
      : '';
    const offset = binarioCompleto.substring(sim.bitsTag + sim.bitsIndice);
    
    return { tag, indice, offset };
  });

  onSimular(request: SimulacaoRequest): void {
    this.simularSubject.next(request);
  }
  
  proximoPasso(): void {
    if (!this.isLastStep()) {
      this.passoAtualIndex.update(i => i + 1);
    }
  }
  
  passoAnterior(): void {
    if (!this.isFirstStep()) {
      this.passoAtualIndex.update(i => i - 1);
    }
  }

  setModo(novoModo: 'livre' | 'desafio'): void {
    this.modo.set(novoModo);
    if (novoModo === 'desafio' && this.desafios().length === 0) {
      this.carregarDesafios();
    }
  }

  carregarDesafios(): void {
    this.carregandoDesafios.set(true);
    this.erroDesafio.set(null);
    this.simuladorService.listarDesafios().pipe(
      tap((lista) => {
        this.desafios.set(lista);
        this.carregandoDesafios.set(false);
        if (lista.length > 0 && !this.desafioSelecionado()) {
          this.selecionarDesafio(lista[0]);
        }
      }),
      catchError((err: HttpErrorResponse) => {
        this.carregandoDesafios.set(false);
        this.erroDesafio.set(err.error?.mensagem || 'Erro ao carregar desafios do servidor');
        return of([]);
      })
    ).subscribe();
  }

  selecionarDesafio(desafio: DesafioCache): void {
    this.desafioSelecionado.set(desafio);
    this.opcaoSelecionada.set(null);
    this.resultadoDesafio.set(null);
    this.erroDesafio.set(null);
    this.simulacao.set(null);
    this.passoAtualIndex.set(0);
  }

  selecionarOpcao(opcaoId: string): void {
    this.opcaoSelecionada.set(opcaoId);
  }

  verificarDesafio(): void {
    const desafio = this.desafioSelecionado();
    const opcao = this.opcaoSelecionada();
    if (!desafio || !opcao) return;

    this.carregandoVerificacao.set(true);
    this.erroDesafio.set(null);

    this.simuladorService.verificarDesafio(desafio.id, { opcaoSelecionadaId: opcao }).pipe(
      tap((resultado) => {
        this.resultadoDesafio.set(resultado);
        this.simulacao.set(resultado.simulacao);
        this.passoAtualIndex.set(0);
        this.carregandoVerificacao.set(false);

        if (resultado.correto && resultado.xpGanho > 0 && this.sessao) {
          this.sessao.atualizarAposResposta(
            resultado.xpTotalAtual,
            resultado.nivelAtual,
            resultado.nomeNivel
          );
        }
      }),
      catchError((err: HttpErrorResponse) => {
        this.carregandoVerificacao.set(false);
        this.erroDesafio.set(err.error?.mensagem || 'Erro ao validar o desafio');
        return of(null);
      })
    ).subscribe();
  }
}
