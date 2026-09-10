import { Component, EventEmitter, Output, Input, inject, DestroyRef } from '@angular/core';
import { MessageService } from 'primeng/api';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { SelectButtonModule } from 'primeng/selectbutton';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { SimulacaoRequest, TipoMapeamento, PoliticaSubstituicao } from '../../models/simulador.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-simulador-form',
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    SelectButtonModule,
    InputTextModule,
    TooltipModule
  ],
  templateUrl: './simulador-form.component.html',
  styleUrls: ['./simulador-form.component.scss']
})
export class SimuladorFormComponent {
  @Input() loading = false;
  @Output() simular = new EventEmitter<SimulacaoRequest>();

  private readonly notificacoes = inject(MessageService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  
  readonly form = this.fb.nonNullable.group({
    tamanhoCacheBytes: [16, Validators.required],
    tamanhoBlocoBytes: [4, Validators.required],
    mapeamento: ['DIRETO' as TipoMapeamento, Validators.required],
    numeroVias: [{ value: null as number | null, disabled: true }],
    substituicao: [{ value: null as PoliticaSubstituicao | null, disabled: true }],
    enderecosStr: ['0, 4, 8, 12, 0, 4', Validators.required]
  });
  
  readonly tamanhoOptions = [
    { label: '8\u00A0B', value: 8 },
    { label: '16\u00A0B', value: 16 },
    { label: '32\u00A0B', value: 32 },
    { label: '64\u00A0B', value: 64 }
  ];

  readonly blocoOptions = [
    { label: '2\u00A0B', value: 2 },
    { label: '4\u00A0B', value: 4 },
    { label: '8\u00A0B', value: 8 },
    { label: '16\u00A0B', value: 16 }
  ];

  readonly mapeamentoOptions = [
    { label: 'Direto', value: 'DIRETO' },
    { label: 'Total.\u00A0Assoc.', value: 'TOTALMENTE_ASSOCIATIVO' },
    { label: 'Conjuntos', value: 'CONJUNTO_ASSOCIATIVO' }
  ];

  readonly viasOptions = [
    { label: '2\u00A0Vias', value: 2 },
    { label: '4\u00A0Vias', value: 4 },
    { label: '8\u00A0Vias', value: 8 }
  ];
  
  readonly politicaOptions = [
    { label: 'LRU', value: 'LRU' },
    { label: 'FIFO', value: 'FIFO' }
  ];
  
  readonly presets = [
    {
      nome: 'Loop Temporal',
      descricao: 'Repetição dos mesmos blocos para observar acertos (hits) por localidade temporal',
      icone: 'pi pi-replay',
      enderecos: '0, 4, 8, 12, 0, 4, 8, 12'
    },
    {
      nome: 'Varredura Linear',
      descricao: 'Acesso sequencial que aproveita a localidade espacial dentro das linhas',
      icone: 'pi pi-arrow-right',
      enderecos: '0, 1, 2, 3, 4, 5, 6, 7, 8, 9'
    },
    {
      nome: 'Conflito (Thrashing)',
      descricao: 'Endereços distantes que colidem no mesmo índice no Mapeamento Direto',
      icone: 'pi pi-bolt',
      enderecos: '0, 16, 32, 0, 16, 32'
    },
    {
      nome: 'Substituição',
      descricao: 'Sequência que força descarte para comparar políticas LRU vs FIFO',
      icone: 'pi pi-sync',
      enderecos: '0, 4, 8, 12, 16, 0, 4, 20'
    }
  ];

  constructor() {
    this.setupFormListeners();
  }

  carregarPreset(enderecos: string): void {
    this.form.controls.enderecosStr.setValue(enderecos);
    this.notificacoes.add({
      severity: 'info',
      summary: 'Preset Aplicado',
      detail: `Sequência carregada no campo de endereços.`,
      life: 2500
    });
  }

  gerarEnderecosAleatorios(quantidade: number = 10, maximo: number = 64): void {
    const enderecos: number[] = [];
    for (let i = 0; i < quantidade; i++) {
      enderecos.push(Math.floor(Math.random() * maximo));
    }
    const sequencia = enderecos.join(', ');
    this.form.controls.enderecosStr.setValue(sequencia);
    this.notificacoes.add({
      severity: 'success',
      summary: 'Endereços Aleatórios Gerados',
      detail: `${quantidade} endereços (0 a ${maximo - 1}) gerados com sucesso.`,
      life: 2500
    });
  }
  
  private setupFormListeners(): void {
    this.form.controls.mapeamento.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((mapeamento: TipoMapeamento) => {
        const numeroViasCtrl = this.form.controls.numeroVias;
        const substituicaoCtrl = this.form.controls.substituicao;
        
        if (mapeamento === 'DIRETO') {
          numeroViasCtrl.disable();
          numeroViasCtrl.setValue(null);
          substituicaoCtrl.disable();
          substituicaoCtrl.setValue(null);
        } else if (mapeamento === 'TOTALMENTE_ASSOCIATIVO') {
          numeroViasCtrl.disable();
          numeroViasCtrl.setValue(null);
          substituicaoCtrl.enable();
          if (!substituicaoCtrl.value) substituicaoCtrl.setValue('LRU');
        } else {
          numeroViasCtrl.enable();
          if (!numeroViasCtrl.value) numeroViasCtrl.setValue(2);
          substituicaoCtrl.enable();
          if (!substituicaoCtrl.value) substituicaoCtrl.setValue('LRU');
        }
      });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    
    const val = this.form.getRawValue();
    
    if (val.tamanhoBlocoBytes > val.tamanhoCacheBytes) {
      this.notificacoes.add({ severity: 'error', summary: 'Erro de Configuração', detail: 'O Bloco não pode ser maior que a Cache!' });
      return;
    }
    
    const enderecosStr = val.enderecosStr || '';
    if (!/^\s*\d+(\s*,\s*\d+)*\s*$/.test(enderecosStr)) {
       this.notificacoes.add({ severity: 'error', summary: 'Formato Inválido', detail: 'Endereços devem ser números separados por vírgula (ex: 0, 4, 8)' });
       return;
    }
    
    const enderecos = enderecosStr
      .split(',')
      .map((e: string) => parseInt(e.trim(), 10))
      .filter((e: number) => !isNaN(e));
      
    const request: SimulacaoRequest = {
      tamanhoCacheBytes: val.tamanhoCacheBytes,
      tamanhoBlocoBytes: val.tamanhoBlocoBytes,
      numeroVias: val.numeroVias,
      mapeamento: val.mapeamento,
      substituicao: val.substituicao,
      enderecos: enderecos
    };
    
    this.simular.emit(request);
  }
}
