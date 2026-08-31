import { Component, inject } from '@angular/core';
import { EstudarStateService } from '../../services/estudar-state.service';
import { AlternativaEstudoResponse } from '../../models/estudar.model';
import { ButtonModule } from 'primeng/button';
import { RadioButtonModule } from 'primeng/radiobutton';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-estudar-resolucao',
  standalone: true,
  imports: [ButtonModule, RadioButtonModule, FormsModule, CardModule, MessageModule],
  template: `
    <!-- Header -->
    <div class="resolucao-header">
      <button class="voltar-btn" (click)="voltar()">
        <i class="pi pi-arrow-left"></i>
        <span>Voltar</span>
      </button>
      <div class="progresso-info">
        <span class="progresso-num">{{ state.questaoAtualIndex() + 1 }}</span>
        <span class="progresso-sep">/</span>
        <span class="progresso-total">{{ state.questoes().length }}</span>
      </div>
    </div>

    @if (state.questaoAtiva(); as q) {
      <!-- Card da Questão -->
      <div class="questao-card">
        <div class="questao-meta">
          <span class="badge-dificuldade" [class]="q.dificuldade.toLowerCase()">{{ q.dificuldade }}</span>
          <span class="badge-xp"><i class="pi pi-star-fill"></i> {{ q.xpBase }} XP</span>
        </div>

        <p class="questao-enunciado">{{ q.enunciado }}</p>

        <!-- Alternativas -->
        <div class="alternativas-lista">
          @for (alt of q.alternativas; track alt.id; let i = $index) {
            <button
              class="alternativa-item"
              [class.selecionada]="classeAlternativa(alt) === 'selecionada'"
              [class.correta]="classeAlternativa(alt) === 'correta'"
              [class.incorreta]="classeAlternativa(alt) === 'incorreta'"
              [class.neutra-esmaecida]="classeAlternativa(alt) === 'neutra-esmaecida'"
              [disabled]="state.respondendo() || state.resultadoResposta() !== null"
              (click)="alternativaSelecionada = alt.id">
              <span class="alternativa-letra">{{ letras[i] }}</span>
              <span class="alternativa-texto">{{ alt.texto }}</span>
              @if (state.resultadoResposta(); as res) {
                @if (alt.id === res.alternativaCorretaId) {
                  <i class="pi pi-check alternativa-icone icone-correto"></i>
                } @else if (alt.id === alternativaSelecionada && !res.correta) {
                  <i class="pi pi-times alternativa-icone icone-incorreto"></i>
                }
              }
            </button>
          }
        </div>

        @if (state.error()) {
          <p-message severity="error" [text]="state.error()!" styleClass="block w-full mt-3"></p-message>
        }

        <!-- Feedback (após responder) -->
        @if (state.resultadoResposta(); as res) {
          <div class="feedback" [class.feedback-correto]="res.correta" [class.feedback-errado]="!res.correta">
            <div class="feedback-header">
              <i class="pi" [class.pi-check-circle]="res.correta" [class.pi-times-circle]="!res.correta"></i>
              <strong>{{ res.correta ? 'Resposta Correta!' : 'Resposta Incorreta' }}</strong>
            </div>
            <p class="feedback-explicacao">{{ res.explicacao }}</p>
            <div class="feedback-xp">
              <i class="pi pi-star-fill"></i>
              +{{ res.xpConcedido }} XP
              <span class="feedback-total">(Total: {{ res.xpTotal }} XP)</span>
            </div>
            @if (res.nivelAtual) {
              <div class="feedback-nivel">
                <i class="pi pi-crown"></i>
                Nível {{ res.nivelAtual }} — {{ res.nomeNivelAtual }}
              </div>
            }
          </div>

          <div class="acao-footer">
            <p-button label="Próxima Questão" icon="pi pi-arrow-right" iconPos="right" styleClass="botao-proximo" (onClick)="proxima()"></p-button>
          </div>
        } @else {
          <div class="acao-footer">
            <p-button
              label="Responder"
              icon="pi pi-check"
              [loading]="state.respondendo()"
              [disabled]="!alternativaSelecionada"
              styleClass="botao-responder"
              (onClick)="responder()">
            </p-button>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    :host { display: block; }

    .resolucao-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1.5rem;
    }

    .voltar-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border: 1px solid #e5edef;
      border-radius: 0.75rem;
      background: #fff;
      color: #667d8c;
      font-size: 0.9rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s;
    }
    .voltar-btn:hover {
      background: #f0f4f7;
      color: #102e45;
      border-color: #c5d0d6;
    }

    .progresso-info {
      display: flex;
      align-items: baseline;
      gap: 0.25rem;
    }
    .progresso-num {
      font-size: 1.75rem;
      font-weight: 800;
      color: #102e45;
      letter-spacing: -0.04em;
    }
    .progresso-sep {
      font-size: 1.1rem;
      color: #c5d0d6;
      font-weight: 700;
    }
    .progresso-total {
      font-size: 1.1rem;
      color: #667d8c;
      font-weight: 600;
    }

    .questao-card {
      background: #fff;
      border: 1px solid #e5edef;
      border-radius: 1rem;
      padding: clamp(1.5rem, 4vw, 2.5rem);
      box-shadow: 0 0.45rem 1.1rem rgba(20, 64, 79, 0.04);
    }

    .questao-meta {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 1.25rem;
    }

    .badge-dificuldade {
      padding: 0.3rem 0.75rem;
      border-radius: 2rem;
      font-size: 0.75rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .badge-dificuldade.facil { background: #ddfae9; color: #19aa62; }
    .badge-dificuldade.medio, .badge-dificuldade.media { background: #fff3cd; color: #856404; }
    .badge-dificuldade.dificil { background: #fde0e0; color: #c63737; }

    .badge-xp {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.3rem 0.75rem;
      border-radius: 2rem;
      font-size: 0.75rem;
      font-weight: 800;
      background: #fff7ef;
      color: #f76c14;
    }

    .questao-enunciado {
      font-size: 1.2rem;
      line-height: 1.7;
      color: #102e45;
      margin: 0 0 2rem;
      font-weight: 500;
    }

    .alternativas-lista {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .alternativa-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      width: 100%;
      padding: 1rem 1.25rem;
      border: 2px solid #e5edef;
      border-radius: 0.85rem;
      background: #fff;
      color: #102e45;
      font-size: 1rem;
      font-weight: 500;
      cursor: pointer;
      text-align: left;
      transition: all 0.15s ease-in-out;
    }

    .alternativa-item:hover:not(:disabled):not(.selecionada) {
      border-color: #a8d4df;
      background: #f6fbfc;
    }

    /* Estado: Selecionada (Antes de responder) */
    .alternativa-item.selecionada {
      border-color: #247ba0;
      background: #e8f4f8;
      color: #102e45;
    }
    .alternativa-item.selecionada .alternativa-letra {
      background: #247ba0;
      color: #fff;
    }

    /* Estado: Correta (Após responder) */
    .alternativa-item.correta {
      border-color: #22c55e !important;
      background: #f0fdf4 !important;
      color: #14532d !important;
      font-weight: 600;
      opacity: 1 !important;
      cursor: default;
    }
    .alternativa-item.correta .alternativa-letra {
      background: #22c55e !important;
      color: #fff !important;
    }
    .alternativa-item.correta .alternativa-texto {
      color: #14532d !important;
    }

    /* Estado: Incorreta (Selecionada pelo usuário e incorreta) */
    .alternativa-item.incorreta {
      border-color: #ef4444 !important;
      background: #fef2f2 !important;
      color: #7f1d1d !important;
      font-weight: 600;
      opacity: 1 !important;
      cursor: default;
    }
    .alternativa-item.incorreta .alternativa-letra {
      background: #ef4444 !important;
      color: #fff !important;
    }
    .alternativa-item.incorreta .alternativa-texto {
      color: #7f1d1d !important;
    }

    /* Estado: Neutra Esmaecida (Demais alternativas após responder) */
    .alternativa-item.neutra-esmaecida {
      opacity: 0.45 !important;
      background: #fafbfc !important;
      border-color: #e5edef !important;
      cursor: default;
    }
    .alternativa-item.neutra-esmaecida .alternativa-letra {
      background: #f0f4f7 !important;
      color: #94a3b8 !important;
    }
    .alternativa-item.neutra-esmaecida .alternativa-texto {
      color: #64748b !important;
    }

    .alternativa-letra {
      display: grid;
      width: 2.25rem;
      height: 2.25rem;
      flex-shrink: 0;
      place-items: center;
      border-radius: 50%;
      background: #f0f4f7;
      color: #667d8c;
      font-weight: 800;
      font-size: 0.85rem;
      transition: all 0.15s;
    }

    .alternativa-texto {
      flex: 1;
      line-height: 1.5;
    }

    .alternativa-icone {
      font-size: 1.2rem;
      font-weight: bold;
      flex-shrink: 0;
    }

    .icone-correto {
      color: #16a34a;
    }

    .icone-incorreto {
      color: #dc2626;
    }

    .feedback {
      margin-top: 1.5rem;
      padding: 1.5rem;
      border-radius: 0.85rem;
      border: 1px solid;
    }
    .feedback-correto {
      background: #f0fdf4;
      border-color: #bbf7d0;
      color: #166534;
    }
    .feedback-errado {
      background: #fef2f2;
      border-color: #fecaca;
      color: #991b1b;
    }
    .feedback-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.15rem;
      margin-bottom: 0.75rem;
    }
    .feedback-header i { font-size: 1.35rem; }
    .feedback-explicacao {
      margin: 0 0 1rem;
      line-height: 1.65;
      font-size: 0.95rem;
      opacity: 0.9;
    }
    .feedback-xp {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      font-weight: 700;
      font-size: 1rem;
    }
    .feedback-xp i { color: #f59e0b; }
    .feedback-total {
      font-weight: 400;
      opacity: 0.7;
      font-size: 0.9rem;
    }
    .feedback-nivel {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      margin-top: 0.5rem;
      font-size: 0.85rem;
      opacity: 0.8;
    }

    .acao-footer {
      display: flex;
      justify-content: flex-end;
      margin-top: 1.5rem;
    }

    :host ::ng-deep .botao-responder,
    :host ::ng-deep .botao-proximo {
      min-height: 3rem;
      padding: 0.7rem 2rem;
      border-radius: 0.75rem;
      font-weight: 700;
      font-size: 1rem;
    }
  `]
})
export class EstudarResolucaoComponent {
  readonly state = inject(EstudarStateService);
  alternativaSelecionada: number | null = null;
  readonly letras = ['A', 'B', 'C', 'D', 'E', 'F'];

  classeAlternativa(alt: AlternativaEstudoResponse): string {
    const res = this.state.resultadoResposta();
    if (!res) {
      return this.alternativaSelecionada === alt.id ? 'selecionada' : '';
    }
    if (alt.id === res.alternativaCorretaId) {
      return 'correta';
    }
    if (alt.id === this.alternativaSelecionada && !res.correta) {
      return 'incorreta';
    }
    return 'neutra-esmaecida';
  }

  responder(): void {
    if (this.alternativaSelecionada) {
      this.state.responder(this.alternativaSelecionada);
    }
  }

  proxima(): void {
    this.alternativaSelecionada = null;
    this.state.proximaQuestao();
  }

  voltar(): void {
    this.alternativaSelecionada = null;
    this.state.voltarParaListagem();
  }
}

