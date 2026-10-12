import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AdminDesafioSimuladorApiService } from '../../services/admin-desafio-simulador-api.service';
import { DesafioSimuladorRequest } from '../../models/admin-desafio.model';

@Component({
  selector: 'app-admin-desafios-simulador-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './admin-desafios-simulador-form.component.html',
  styleUrls: ['./admin-desafios-simulador-form.component.scss']
})
export class AdminDesafiosSimuladorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly adminDesafioService = inject(AdminDesafioSimuladorApiService);
  private readonly messageService = inject(MessageService);

  form!: FormGroup;
  editando = signal<boolean>(false);
  desafioId = signal<string | null>(null);
  salvando = signal<boolean>(false);

  dificuldades = [
    { label: 'Fácil', value: 'FACIL' },
    { label: 'Médio', value: 'MEDIO' },
    { label: 'Difícil', value: 'DIFICIL' }
  ];

  mapeamentos = [
    { label: 'Direto', value: 'DIRETO' },
    { label: 'Totalmente Associativo', value: 'TOTALMENTE_ASSOCIATIVO' },
    { label: 'Conjunto Associativo', value: 'CONJUNTO_ASSOCIATIVO' }
  ];

  substituicoes = [
    { label: 'Nenhuma', value: null },
    { label: 'LRU', value: 'LRU' },
    { label: 'FIFO', value: 'FIFO' }
  ];

  ngOnInit(): void {
    this.iniciarFormulario();
    this.verificarEdicao();
  }

  private iniciarFormulario(): void {
    this.form = this.fb.group({
      titulo: ['', Validators.required],
      descricao: ['', Validators.required],
      dificuldade: ['FACIL', Validators.required],
      xpRecompensa: [100, [Validators.required, Validators.min(0)]],
      configuracao: this.fb.group({
        tamanhoCacheBytes: [64, [Validators.required, Validators.min(1)]],
        tamanhoBlocoBytes: [16, [Validators.required, Validators.min(1)]],
        numeroVias: [null],
        mapeamento: ['DIRETO', Validators.required],
        substituicao: [null],
        enderecosTexto: ['', Validators.required]
      }),
      pergunta: ['', Validators.required],
      opcoes: this.fb.array([]),
      opcaoCorretaId: ['', Validators.required],
      dica: [''],
      explicacao: ['', Validators.required]
    });

    // Add 4 default options
    this.adicionarOpcao('A', '');
    this.adicionarOpcao('B', '');
    this.adicionarOpcao('C', '');
    this.adicionarOpcao('D', '');
  }

  get opcoesFormArray() {
    return this.form.get('opcoes') as FormArray;
  }

  adicionarOpcao(id: string = '', texto: string = ''): void {
    const opcaoGroup = this.fb.group({
      id: [id, Validators.required],
      texto: [texto, Validators.required]
    });
    this.opcoesFormArray.push(opcaoGroup);
  }

  removerOpcao(index: number): void {
    this.opcoesFormArray.removeAt(index);
  }

  private verificarEdicao(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando.set(true);
      this.desafioId.set(id);
      this.carregarDesafio(id);
    }
  }

  private carregarDesafio(id: string): void {
    this.adminDesafioService.obterPorId(id).subscribe({
      next: (desafio) => {
        // Clear default options
        while (this.opcoesFormArray.length !== 0) {
          this.opcoesFormArray.removeAt(0);
        }
        
        // Add options from desafio
        desafio.opcoes.forEach(op => {
          this.adicionarOpcao(op.id, op.texto);
        });

        this.form.patchValue({
          ...desafio,
          configuracao: {
            ...desafio.configuracao,
            enderecosTexto: desafio.configuracao.enderecos.join(', ')
          }
        });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: 'Desafio não encontrado.' });
        this.router.navigate(['/admin/desafios']);
      }
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messageService.add({ severity: 'warn', summary: 'Atenção', detail: 'Preencha todos os campos obrigatórios.' });
      return;
    }

    this.salvando.set(true);
    const formValue = this.form.value;

    const enderecosStr: string = formValue.configuracao.enderecosTexto;
    const enderecosArr = enderecosStr.split(',').map(e => e.trim()).filter(e => e !== '').map(e => Number(e));

    const payload: DesafioSimuladorRequest = {
      ...formValue,
      id: this.desafioId() ?? undefined,
      configuracao: {
        tamanhoCacheBytes: formValue.configuracao.tamanhoCacheBytes,
        tamanhoBlocoBytes: formValue.configuracao.tamanhoBlocoBytes,
        numeroVias: formValue.configuracao.numeroVias,
        mapeamento: formValue.configuracao.mapeamento,
        substituicao: formValue.configuracao.substituicao,
        enderecos: enderecosArr
      }
    };

    const request$ = this.editando() 
      ? this.adminDesafioService.atualizar(this.desafioId()!, payload)
      : this.adminDesafioService.criar(payload);

    request$.subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Sucesso', detail: 'Desafio salvo com sucesso!' });
        setTimeout(() => this.router.navigate(['/admin/desafios']), 1500);
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: 'Falha ao salvar o desafio.' });
        this.salvando.set(false);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/admin/desafios']);
  }
}
