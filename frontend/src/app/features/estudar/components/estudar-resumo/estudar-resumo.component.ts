import { Component, inject } from '@angular/core';
import { EstudarStateService } from '../../services/estudar-state.service';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-estudar-resumo',
  standalone: true,
  imports: [ButtonModule, CardModule],
  template: `
    <div class="flex justify-content-center mt-5">
      <p-card styleClass="text-center p-4 max-w-30rem">
        <i class="pi pi-check-circle text-6xl text-green-500 mb-4"></i>
        <h2 class="mt-0 mb-3">Sessão Concluída!</h2>
        <p class="text-600 mb-4">Você terminou todas as questões desta lista.</p>
        
        <div class="grid text-left mb-4 bg-gray-50 border-round p-3">
          <div class="col-12 flex justify-content-between border-bottom-1 surface-border py-2">
            <span class="font-bold">Questões Respondidas:</span>
            <span>{{ state.sessaoResumo().questoesRespondidas }}</span>
          </div>
          <div class="col-12 flex justify-content-between border-bottom-1 surface-border py-2">
            <span class="font-bold">Acertos:</span>
            <span class="text-green-600">{{ state.sessaoResumo().acertos }}</span>
          </div>
          <div class="col-12 flex justify-content-between py-2">
            <span class="font-bold">XP Acumulado:</span>
            <span class="text-yellow-600 font-bold">+{{ state.sessaoResumo().xpTotalGanho }}</span>
          </div>
        </div>
        
        <p-button label="Voltar para a Listagem" icon="pi pi-list" styleClass="w-full" (onClick)="state.voltarParaListagem()"></p-button>
      </p-card>
    </div>
  `
})
export class EstudarResumoComponent {
  readonly state = inject(EstudarStateService);
}
