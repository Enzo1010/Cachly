import type { ChartData, ChartOptions } from 'chart.js';
import { SimulacaoResponse, PassoSimulacaoResponse } from '../models/simulador.model';

export function criarDadosRosca(simulacao: SimulacaoResponse): ChartData<'doughnut'> {
  return {
    labels: ['Hits', 'Misses'],
    datasets: [
      {
        data: [simulacao.taxaHit, simulacao.taxaMiss],
        backgroundColor: ['#22c55e', '#ef4444'],
        hoverBackgroundColor: ['#16a34a', '#dc2626']
      }
    ]
  };
}

export function criarDadosLinha(simulacao: SimulacaoResponse, passoAtualIndex: number): ChartData<'line'> {
  const labels: string[] = [];
  const hitData: number[] = [];
  let hitsAtuais = 0;

  if (!simulacao || !simulacao.passos || simulacao.passos.length === 0) {
    return { labels: [], datasets: [] };
  }

  const limite = Math.min(passoAtualIndex, simulacao.passos.length - 1);
  for (let i = 0; i <= limite; i++) {
    labels.push(`P${i + 1}`);
    if (simulacao.passos[i]?.hit) hitsAtuais++;
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
}

export const OPCOES_GRAFICO_ROSCA: ChartOptions<'doughnut'> = {
  cutout: '70%',
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        color: '#64748b',
        font: { size: 11, weight: 'bold' },
        padding: 12,
        boxWidth: 10,
        boxHeight: 10
      }
    }
  }
};

export const OPCOES_GRAFICO_LINHA: ChartOptions<'line'> = {
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
