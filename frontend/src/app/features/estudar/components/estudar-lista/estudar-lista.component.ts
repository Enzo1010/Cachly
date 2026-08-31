import { Component, inject } from '@angular/core';
import { EstudarStateService } from '../../services/estudar-state.service';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';
import { SlicePipe } from '@angular/common';

@Component({
  selector: 'app-estudar-lista',
  standalone: true,
  imports: [ButtonModule, SkeletonModule, CardModule, MessageModule, SlicePipe],
  template: `
    @if (!state.categoriaFiltro()) {
      <!-- TELA 1: LISTA DE CATEGORIAS -->
      <div class="lista-header">
        <div>
          <h2 class="lista-titulo">O que vamos estudar hoje?</h2>
          <p class="lista-subtitulo">Escolha um tópico para começar a treinar e ganhar XP</p>
        </div>
      </div>

      <div class="categorias-grid">
        @for (cat of state.categorias(); track cat.id) {
          <div class="categoria-card" (click)="selecionarCategoria(cat.id)">
            <div class="cat-icon"><i class="pi pi-book"></i></div>
            <div class="cat-info">
              <h3>{{ cat.nome }}</h3>
              <p>{{ cat.descricao || 'Resolva questões sobre este tema' }}</p>
            </div>
            <i class="pi pi-chevron-right cat-arrow"></i>
          </div>
        }
      </div>
    } @else {
      <!-- TELA 2: LISTA DE QUESTÕES DA CATEGORIA -->
      <div class="lista-header mb-2">
        <button class="voltar-btn" (click)="selecionarCategoria(null)">
          <i class="pi pi-arrow-left"></i>
          <span>Voltar aos tópicos</span>
        </button>
      </div>

      <div class="categoria-header-destaque">
        <h2>{{ categoriaAtual?.nome }}</h2>
        <p>{{ categoriaAtual?.descricao }}</p>
      </div>

      @if (state.error()) {
        <p-message severity="error" [text]="state.error()!"></p-message>
      }

      @if (state.loading()) {
        <div class="skeleton-list">
          @for (i of [1, 2, 3]; track i) {
            <div class="skeleton-card">
              <p-skeleton height="80px" styleClass="border-round-xl"></p-skeleton>
            </div>
          }
        </div>
      } @else {
        @if (state.questoes().length === 0) {
          <div class="empty-state">
            <i class="pi pi-inbox"></i>
            <h3>Nenhuma questão disponível</h3>
            <p>Ainda não há questões cadastradas para este tópico.</p>
          </div>
        } @else {
          <!-- CTA - Iniciar Estudo -->
          <div class="cta-card">
            <div class="cta-content">
              <div class="cta-icon"><i class="pi pi-play-circle"></i></div>
              <div>
                <p class="cta-contagem"><strong>{{ state.questoes().length }}</strong> questões prontas</p>
                <p class="cta-desc">Responda as questões para ganhar XP e subir de nível</p>
              </div>
            </div>
            <button class="cta-button" (click)="state.iniciarEstudo()">
              <i class="pi pi-play"></i>
              Iniciar Estudo
            </button>
          </div>

          <!-- Lista de questões (preview opcional para ver o que tem) -->
          <div class="questoes-lista">
            @for (q of state.questoes(); track q.id; let i = $index) {
              <div class="questao-preview">
                <div class="questao-numero">{{ i + 1 }}</div>
                <div class="questao-body">
                  <div class="questao-badges">
                    <span class="badge-dificuldade" [class]="q.dificuldade.toLowerCase()">{{ q.dificuldade }}</span>
                    <span class="badge-xp"><i class="pi pi-star-fill"></i> +{{ q.xpBase }} XP</span>
                  </div>
                  <p class="questao-texto">{{ q.enunciado | slice:0:150 }}{{ q.enunciado.length > 150 ? '...' : '' }}</p>
                </div>
              </div>
            }
          </div>
        }
      }
    }
  `,
  styles: [`
    :host { display: block; }

    .lista-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
    }

    .lista-titulo {
      margin: 0;
      font-size: clamp(1.6rem, 3vw, 2rem);
      font-weight: 800;
      color: #102e45;
      letter-spacing: -0.04em;
    }

    .lista-subtitulo {
      margin: 0.35rem 0 0;
      color: #667d8c;
      font-size: 0.95rem;
    }

    /* Tela 1: Grid de Categorias */
    .categorias-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.25rem;
    }

    .categoria-card {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      padding: 1.5rem;
      background: #fff;
      border: 1px solid #e5edef;
      border-radius: 1rem;
      box-shadow: 0 0.45rem 1.1rem rgba(20, 64, 79, 0.04);
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .categoria-card:hover {
      border-color: #a8d4df;
      box-shadow: 0 0.8rem 2rem rgba(20, 64, 79, 0.08);
      transform: translateY(-2px);
    }
    .categoria-card:hover .cat-icon {
      background: #006494;
      color: #fff;
    }

    .cat-icon {
      display: grid;
      width: 3.5rem;
      height: 3.5rem;
      flex-shrink: 0;
      place-items: center;
      border-radius: 0.85rem;
      background: #f0f4f7;
      color: #006494;
      font-size: 1.5rem;
      transition: all 0.2s;
    }

    .cat-info {
      flex: 1;
      min-width: 0;
    }
    .cat-info h3 {
      margin: 0 0 0.25rem;
      color: #102e45;
      font-size: 1.1rem;
      font-weight: 700;
    }
    .cat-info p {
      margin: 0;
      color: #667d8c;
      font-size: 0.85rem;
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .cat-arrow {
      color: #c5d0d6;
      font-size: 1rem;
    }

    /* Tela 2: Detalhes da Categoria */
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

    .categoria-header-destaque {
      margin-bottom: 2rem;
    }
    .categoria-header-destaque h2 {
      margin: 0 0 0.5rem;
      font-size: 2rem;
      font-weight: 800;
      color: #102e45;
      letter-spacing: -0.04em;
    }
    .categoria-header-destaque p {
      margin: 0;
      font-size: 1.05rem;
      color: #667d8c;
      line-height: 1.5;
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #667d8c;
    }
    .empty-state i {
      font-size: 3.5rem;
      margin-bottom: 1rem;
      color: #c5d0d6;
    }
    .empty-state h3 {
      margin: 0 0 0.5rem;
      color: #102e45;
      font-size: 1.25rem;
    }
    .empty-state p {
      margin: 0;
      font-size: 0.95rem;
    }

    .skeleton-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      margin-top: 1rem;
    }

    /* CTA Card */
    .cta-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 1.5rem;
      padding: 1.5rem 2rem;
      margin-bottom: 2rem;
      background: linear-gradient(105deg, #112f46, #006fa0);
      border-radius: 1rem;
      box-shadow: 0 0.8rem 1.4rem rgba(3, 41, 61, 0.15);
    }
    .cta-content {
      display: flex;
      align-items: center;
      gap: 1.25rem;
    }
    .cta-icon {
      display: grid;
      width: 3.5rem;
      height: 3.5rem;
      place-items: center;
      border-radius: 50%;
      background: rgba(255,255,255,0.12);
      color: #fff;
      font-size: 1.75rem;
    }
    .cta-contagem {
      margin: 0;
      color: #fff;
      font-size: 1.15rem;
    }
    .cta-desc {
      margin: 0.2rem 0 0;
      color: #d9e8ef;
      font-size: 0.85rem;
    }
    .cta-button {
      display: inline-flex;
      align-items: center;
      gap: 0.6rem;
      padding: 0.85rem 2rem;
      border: none;
      border-radius: 0.7rem;
      background: #fff;
      color: #006fa0;
      font-weight: 800;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.15s;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    .cta-button:hover {
      background: #f0f8ff;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    /* Questões Lista */
    .questoes-lista {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .questao-preview {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      padding: 1.25rem 1.5rem;
      background: #fff;
      border: 1px solid #e5edef;
      border-radius: 1rem;
      box-shadow: 0 0.45rem 1.1rem rgba(20, 64, 79, 0.04);
      transition: all 0.15s;
    }

    .questao-numero {
      display: grid;
      width: 2.5rem;
      height: 2.5rem;
      flex-shrink: 0;
      place-items: center;
      border-radius: 50%;
      background: #f0f4f7;
      color: #667d8c;
      font-weight: 800;
      font-size: 0.95rem;
    }

    .questao-body {
      flex: 1;
      min-width: 0;
    }

    .questao-badges {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }

    .badge-dificuldade {
      padding: 0.2rem 0.6rem;
      border-radius: 2rem;
      font-size: 0.7rem;
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
      gap: 0.25rem;
      font-size: 0.75rem;
      font-weight: 700;
      color: #f76c14;
    }

    .questao-texto {
      margin: 0;
      color: #102e45;
      font-size: 0.95rem;
      line-height: 1.5;
    }

    @media (max-width: 600px) {
      .cta-card {
        flex-direction: column;
        text-align: center;
        padding: 1.5rem;
      }
      .cta-content {
        flex-direction: column;
      }
      .cta-button { width: 100%; justify-content: center; }
      
      .categorias-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class EstudarListaComponent {
  readonly state = inject(EstudarStateService);

  get categoriaAtual() {
    const id = this.state.categoriaFiltro();
    if (!id) return null;
    return this.state.categorias().find(c => c.id === id) || null;
  }

  selecionarCategoria(id: number | null): void {
    this.state.aplicarFiltroCategoria(id);
  }
}
