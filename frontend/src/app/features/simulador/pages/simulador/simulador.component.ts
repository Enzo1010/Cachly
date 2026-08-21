import { Component, signal, computed, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { MessageModule } from 'primeng/message';
import { TooltipModule } from 'primeng/tooltip';
import { ChartModule } from 'primeng/chart';
import { SimuladorFormComponent } from '../../components/simulador-form/simulador-form.component';
import { SimuladorTabelaComponent } from '../../components/simulador-tabela/simulador-tabela.component';
import { SimulacaoRequest, SimulacaoResponse, PassoSimulacaoResponse } from '../../models/simulador.model';
import { SimuladorCacheService } from '../../services/simulador-cache.service';
import { HttpErrorResponse } from '@angular/common/http';

interface BreakdownBinario {
  tag: string;
  indice: string;
  offset: string;
}

@Component({
  selector: 'app-simulador-page',
  standalone: true,
  imports: [
    CommonModule,
    PanelModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    TagModule,
    DividerModule,
    MessageModule,
    TooltipModule,
    ChartModule,
    SimuladorFormComponent,
    SimuladorTabelaComponent
  ],
  templateUrl: './simulador.component.html',
  styleUrls: ['./simulador.component.scss']
})
export class SimuladorPageComponent {
  private readonly simuladorService = inject(SimuladorCacheService);
  
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  simulacao = signal<SimulacaoResponse | null>(null);
  passoAtualIndex = signal<number>(0);
  
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

    // Aplica os deltas atǸ o passo atual
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

  // Chart Data
  doughnutData = computed(() => {
    const sim = this.simulacao();
    if (!sim) return null;
    return {
      labels: ['Hits', 'Misses'],
      datasets: [
        {
          data: [sim.taxaHit, sim.taxaMiss],
          backgroundColor: ['#22c55e', '#ef4444'],
          hoverBackgroundColor: ['#16a34a', '#dc2626']
        }
      ]
    };
  });

  lineData = computed(() => {
    const sim = this.simulacao();
    const currentIndex = this.passoAtualIndex();
    if (!sim) return null;
    
    const labels = [];
    const hitData = [];
    let hitsAtuais = 0;

    for (let i = 0; i <= currentIndex; i++) {
      labels.push(`P${i + 1}`);
      if (sim.passos[i].hit) hitsAtuais++;
      hitData.push(hitsAtuais);
    }

    return {
      labels,
      datasets: [
        {
          label: 'Acertos Acumulados',
          data: hitData,
          fill: true,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.2)',
          tension: 0.4
        }
      ]
    };
  });

  chartOptions = {
    cutout: '70%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#64748b',
          font: { size: 11, weight: '600' },
          padding: 12,
          boxWidth: 10,
          boxHeight: 10
        }
      }
    }
  };

  lineChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      x: {
        grid: { color: 'rgba(0,0,0,0.04)' },
        ticks: { color: '#94a3b8', font: { size: 10 } }
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(0,0,0,0.04)' },
        ticks: { color: '#94a3b8', font: { size: 10 }, stepSize: 1 }
      }
    }
  };
  
  onSimular(request: SimulacaoRequest): void {
    this.loading.set(true);
    this.error.set(null);
    this.simulacao.set(null);
    this.passoAtualIndex.set(0);
    
    this.simuladorService.executarSimulacao(request).subscribe({
      next: (response) => {
        this.simulacao.set(response);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(err.error?.mensagem || 'Erro ao comunicar com o servidor');
      }
    });
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
