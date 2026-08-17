import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EstadoLinhaCacheResponse } from '../../models/simulador.model';

interface ConjuntoCache {
  id: number;
  linhas: EstadoLinhaCacheResponse[];
}

@Component({
  selector: 'app-simulador-tabela',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './simulador-tabela.component.html',
  styleUrls: ['./simulador-tabela.component.scss']
})
export class SimuladorTabelaComponent implements OnChanges {
  @Input() estadoCache: EstadoLinhaCacheResponse[] = [];
  @Input() blocoAlterado: number | null = null;
  @Input() isHit: boolean = false;
  
  conjuntos: ConjuntoCache[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['estadoCache'] && this.estadoCache) {
      this.agruparPorConjunto();
    }
  }

  private agruparPorConjunto(): void {
    const mapa = new Map<number, EstadoLinhaCacheResponse[]>();
    
    this.estadoCache.forEach(linha => {
      const setId = linha.conjuntoIndex !== null ? linha.conjuntoIndex : 0;
      if (!mapa.has(setId)) {
        mapa.set(setId, []);
      }
      mapa.get(setId)!.push(linha);
    });
    
    this.conjuntos = Array.from(mapa.entries()).map(([id, linhas]) => ({
      id,
      linhas: linhas.sort((a, b) => a.indiceLinha - b.indiceLinha)
    })).sort((a, b) => a.id - b.id);
  }

  isLinhaAlterada(indiceLinha: number): boolean {
    return this.blocoAlterado === indiceLinha;
  }
}
