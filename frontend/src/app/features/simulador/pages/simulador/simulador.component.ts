import { Component, computed, inject, signal } from '@angular/core';
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
import { SimuladorGuiaComponent, AbaGuia } from '../../components/simulador-guia/simulador-guia.component';
import { SimuladorStateService } from '../../services/simulador-state.service';
import {
  criarDadosRosca,
  criarDadosLinha,
  OPCOES_GRAFICO_ROSCA,
  OPCOES_GRAFICO_LINHA
} from '../../utils/simulador-chart.utils';

@Component({
  selector: 'app-simulador-page',
  standalone: true,
  imports: [
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
    SimuladorTabelaComponent,
    SimuladorGuiaComponent
  ],
  providers: [SimuladorStateService],
  templateUrl: './simulador.component.html',
  styleUrls: ['./simulador.component.scss']
})
export class SimuladorPageComponent {
  readonly state = inject(SimuladorStateService);

  readonly guiaAberto = signal<boolean>(false);
  readonly abaAtivaGuia = signal<AbaGuia>('aritmetica');
  readonly destaqueCampoGuia = signal<'tag' | 'indice' | 'offset' | null>(null);

  readonly opcoesGraficoRosca = OPCOES_GRAFICO_ROSCA;
  readonly opcoesGraficoLinha = OPCOES_GRAFICO_LINHA;

  doughnutData = computed(() => {
    const sim = this.state.simulacao();
    return sim ? criarDadosRosca(sim) : null;
  });

  lineData = computed(() => {
    const sim = this.state.simulacao();
    const currentIndex = this.state.passoAtualIndex();
    return sim ? criarDadosLinha(sim, currentIndex) : null;
  });

  abrirGuia(aba: AbaGuia = 'aritmetica', destaque: 'tag' | 'indice' | 'offset' | null = null): void {
    this.abaAtivaGuia.set(aba);
    this.destaqueCampoGuia.set(destaque);
    this.guiaAberto.set(true);
  }

  abrirGuiaCampo(campo: 'tag' | 'indice' | 'offset'): void {
    this.abrirGuia('aritmetica', campo);
  }
}