import { Component, EventEmitter, Output, Input, inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { SelectButtonModule } from 'primeng/selectbutton';
import { InputTextModule } from 'primeng/inputtext';
import { SimulacaoRequest, TipoMapeamento, PoliticaSubstituicao } from '../../models/simulador.model';

@Component({
  selector: 'app-simulador-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    SelectButtonModule,
    InputTextModule
  ],
  templateUrl: './simulador-form.component.html',
  styleUrls: ['./simulador-form.component.scss']
})
export class SimuladorFormComponent {
  @Input() loading = false;

  private readonly notificacoes = inject(MessageService);
  @Output() simular = new EventEmitter<SimulacaoRequest>();
  
  private readonly fb = inject(FormBuilder);
  
  form: FormGroup;
  
  tamanhoOptions = [
    { label: '8 B', value: 8 },
    { label: '16 B', value: 16 },
    { label: '32 B', value: 32 },
    { label: '64 B', value: 64 }
  ];

  blocoOptions = [
    { label: '2 B', value: 2 },
    { label: '4 B', value: 4 },
    { label: '8 B', value: 8 },
    { label: '16 B', value: 16 }
  ];

  mapeamentoOptions = [
    { label: 'Direto', value: 'DIRETO' },
    { label: 'Total. Assoc.', value: 'TOTALMENTE_ASSOCIATIVO' },
    { label: 'Conjuntos', value: 'CONJUNTO_ASSOCIATIVO' }
  ];

  viasOptions = [
    { label: '2 Vias', value: 2 },
    { label: '4 Vias', value: 4 },
    { label: '8 Vias', value: 8 }
  ];
  
  politicaOptions = [
    { label: 'LRU', value: 'LRU' },
    { label: 'FIFO', value: 'FIFO' }
  ];
  
  constructor() {
    this.form = this.fb.group({
      tamanhoCacheBytes: [16, Validators.required],
      tamanhoBlocoBytes: [4, Validators.required],
      mapeamento: ['DIRETO', Validators.required],
      numeroVias: [{ value: null, disabled: true }],
      substituicao: [{ value: null, disabled: true }],
      enderecosStr: ['0, 4, 8, 12, 0, 4', Validators.required]
    });
    
    this.setupFormListeners();
  }
  
  private setupFormListeners(): void {
    this.form.get('mapeamento')?.valueChanges.subscribe((mapeamento: TipoMapeamento) => {
      const numeroViasCtrl = this.form.get('numeroVias');
      const substituicaoCtrl = this.form.get('substituicao');
      
      if (mapeamento === 'DIRETO') {
        numeroViasCtrl?.disable();
        numeroViasCtrl?.setValue(null);
        substituicaoCtrl?.disable();
        substituicaoCtrl?.setValue(null);
      } else if (mapeamento === 'TOTALMENTE_ASSOCIATIVO') {
        numeroViasCtrl?.disable();
        numeroViasCtrl?.setValue(null);
        substituicaoCtrl?.enable();
        if (!substituicaoCtrl?.value) substituicaoCtrl?.setValue('LRU');
      } else {
        numeroViasCtrl?.enable();
        if (!numeroViasCtrl?.value) numeroViasCtrl?.setValue(2);
        substituicaoCtrl?.enable();
        if (!substituicaoCtrl?.value) substituicaoCtrl?.setValue('LRU');
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
