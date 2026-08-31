import { Injectable, signal, computed, inject, DestroyRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SessaoService } from '../../../core/autenticacao/sessao.service';
import { EstudarApiService } from './estudar-api.service';
import {
  CategoriaResponse,
  QuestaoEstudoResponse,
  RespostaResponse
} from '../models/estudar.model';

// LIMITAÇÃO CONHECIDA:
// A orquestração do módulo "Estudar" utiliza uma rota única e alterna as visões
// via Signals ('LISTA', 'RESOLUCAO', 'RESUMO') para melhorar a fluidez. 
// A consequência dessa arquitetura é que, se o usuário der F5 (refresh) enquanto
// resolve uma questão, o estado do Signal é reiniciado e ele voltará à listagem.
// Uma futura melhoria pode envolver roteamento via parâmetro (/estudar/:id) 
// caso o recarregamento na mesma questão se torne um requisito de produto.
export type EstudarTela = 'LISTA' | 'RESOLUCAO' | 'RESUMO';

@Injectable()
export class EstudarStateService {
  private readonly api = inject(EstudarApiService);
  private readonly sessao = inject(SessaoService);
  private readonly destroyRef = inject(DestroyRef);

  // Estados Base
  readonly telaAtual = signal<EstudarTela>('LISTA');
  readonly categorias = signal<CategoriaResponse[]>([]);
  readonly questoes = signal<QuestaoEstudoResponse[]>([]);
  
  // Controle de Interface
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly respondendo = signal<boolean>(false);
  
  // Filtro
  readonly categoriaFiltro = signal<number | null>(null);
  
  // Resolução e Progresso
  readonly questaoAtualIndex = signal<number>(0);
  readonly resultadoResposta = signal<RespostaResponse | null>(null);
  
  // Resumo da Sessão (Opção B - Comportamento de Fim de Lista)
  readonly sessaoResumo = signal({
    questoesRespondidas: 0,
    acertos: 0,
    xpTotalGanho: 0
  });

  // Computeds
  readonly questaoAtiva = computed(() => {
    const arr = this.questoes();
    const idx = this.questaoAtualIndex();
    if (idx >= 0 && idx < arr.length) {
      return arr[idx];
    }
    return null;
  });

  carregarCategorias(): void {
    this.api.listarCategorias().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (res) => this.categorias.set(res),
      error: () => this.error.set('Não foi possível carregar as categorias.')
    });
  }

  aplicarFiltroCategoria(id: number | null): void {
    this.categoriaFiltro.set(id);
    this.carregarQuestoes();
  }

  carregarQuestoes(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listarQuestoes(this.categoriaFiltro() || undefined).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (res) => {
        this.questoes.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erro ao buscar as questões disponíveis.');
        this.loading.set(false);
      }
    });
  }

  iniciarEstudo(): void {
    if (this.questoes().length > 0) {
      this.questaoAtualIndex.set(0);
      this.resultadoResposta.set(null);
      this.sessaoResumo.set({ questoesRespondidas: 0, acertos: 0, xpTotalGanho: 0 });
      this.telaAtual.set('RESOLUCAO');
    } else {
      this.error.set('Nenhuma questão disponível para iniciar.');
    }
  }

  responder(alternativaId: number): void {
    const questao = this.questaoAtiva();
    if (!questao || this.respondendo()) return;

    this.respondendo.set(true);
    this.error.set(null);

    this.api.responderQuestao(questao.id, alternativaId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (res) => {
        this.resultadoResposta.set(res);
        this.respondendo.set(false);
        
        // Atualiza resumo
        this.sessaoResumo.update(r => ({
          questoesRespondidas: r.questoesRespondidas + 1,
          acertos: r.acertos + (res.correta ? 1 : 0),
          xpTotalGanho: r.xpTotalGanho + res.xpConcedido
        }));
        
        // Atualiza sessão global
        this.sessao.atualizarAposResposta(res.xpTotal, res.nivelAtual, res.nomeNivelAtual);
      },
      error: (err: HttpErrorResponse) => {
        this.respondendo.set(false);
        
        // Tratamento de Validação 400 Bad Request que oculta detalhes no campos
        if (err.status === 400 && err.error?.campos) {
          const chaves = Object.keys(err.error.campos);
          if (chaves.length > 0) {
            const mensagens = chaves.map(c => err.error.campos[c]);
            this.error.set('Erro de Validação: ' + mensagens.join(' | '));
            return;
          }
        }
        
        this.error.set(err.error?.mensagem || 'Erro desconhecido ao responder a questão.');
      }
    });
  }

  proximaQuestao(): void {
    const proximoIndex = this.questaoAtualIndex() + 1;
    if (proximoIndex < this.questoes().length) {
      this.questaoAtualIndex.set(proximoIndex);
      this.resultadoResposta.set(null);
    } else {
      // Fim da Lista: Mostra o Resumo (Opção B)
      this.telaAtual.set('RESUMO');
    }
  }

  voltarParaListagem(): void {
    this.telaAtual.set('LISTA');
    this.questaoAtualIndex.set(0);
    this.resultadoResposta.set(null);
    // Recarrega questoes (pode ter mudado XP ou status delas)
    this.carregarQuestoes();
  }
}