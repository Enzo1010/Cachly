import { Component, input, model, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dialog } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { SimulacaoResponse } from '../../models/simulador.model';

export type AbaGuia = 'aritmetica' | 'mapeamento' | 'substituicao' | 'hitmiss';

@Component({
  selector: 'app-simulador-guia',
  standalone: true,
  imports: [
    CommonModule,
    Dialog,
    ButtonModule,
    TagModule,
    DividerModule
  ],
  templateUrl: './simulador-guia.component.html',
  styleUrls: ['./simulador-guia.component.scss']
})
export class SimuladorGuiaComponent {
  readonly visivel = model<boolean>(false);
  readonly abaAtiva = model<AbaGuia>('aritmetica');
  readonly simulacao = input<SimulacaoResponse | null>(null);
  readonly destaqueCampo = input<'tag' | 'indice' | 'offset' | null>(null);

  readonly fechar = output<void>();

  selecionarAba(aba: AbaGuia): void {
    this.abaAtiva.set(aba);
  }

  aoFechar(): void {
    this.visivel.set(false);
    this.fechar.emit();
  }
}