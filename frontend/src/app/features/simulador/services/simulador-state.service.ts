import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, exhaustMap, tap, catchError, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SimulacaoRequest, SimulacaoResponse, PassoSimulacaoResponse } from '../models/simulador.model';
import { SimuladorCacheService } from './simulador-cache.service';

interface BreakdownBinario {
  tag: string;
  indice: string;
  offset: string;
}

@Injectable()
export class SimuladorStateService {
  private readonly simuladorService = inject(SimuladorCacheService);
  private readonly simularSubject = new Subject<SimulacaoRequest>();

  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  simulacao = signal<SimulacaoResponse | null>(null);
  passoAtualIndex = signal<number>(0);
  sidebarCollapsed = signal<boolean>(false);

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
    if (!sim) return [];

    const totalLinhas = sim.totalLinhas;
    const vias = sim.totalConjuntos > 0 ? totalLinhas / sim.totalConjuntos : totalLinhas;
    
    // Inicializa a cache vazia
    const cache = Array.from({ length: totalLinhas }, (_, i) => ({
      indiceLinha: i,
      conjuntoIndex: sim.totalConjuntos > 1 ? Math.floor(i / vias) : (sim.totalConjuntos === 1 ? 0 : null),
      valida: false,
      tag: null as number | null
    }));

    // Aplica os deltas até o passo atual
    for (let i = 0; i <= currentIndex; i++) {
      const delta = sim.passos[i].deltaLinha;
      if (delta) {
        cache[delta.indiceLinha] = { ...delta };
      }
    }

    return cache;
  });
  
  isFirstStep = computed<boolean>(() => this.passoAtualIndex() === 0);
  isLastStep = computed<boolean>(() => {
    const sim = this.simulacao();
    return sim ? this.passoAtualIndex() === sim.passos.length - 1 : true;
  });

  totalBits = computed<number>(() => {
    const sim = this.simulacao();
    if (!sim) return 0;
    return sim.bitsTag + sim.bitsIndice + sim.bitsOffset;
  });

  binarioBreakdown = computed<BreakdownBinario | null>(() => {
    const sim = this.simulacao();
    const passo = this.passoAtual();
    if (!sim || !passo) return null;
    
    const binarioCompleto = passo.endereco.toString(2).padStart(this.totalBits(), '0');
    
    const tag = binarioCompleto.substring(0, sim.bitsTag);
    const indice = binarioCompleto.substring(sim.bitsTag, sim.bitsTag + sim.bitsIndice);
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
}
