import { Component, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EstudarStateService } from '../../services/estudar-state.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-estudar-resumo',
  standalone: true,
  imports: [ButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="resumo-wrapper" aria-labelledby="titulo-resumo-sessao">
      <article class="resumo-card">
        <div class="resumo-icone" aria-hidden="true">
          <i class="pi pi-trophy"></i>
        </div>

        <h2 id="titulo-resumo-sessao" class="resumo-titulo">Sessão Concluída!</h2>
        <p class="resumo-subtitulo">
          Você finalizou a lista de questões deste tópico. Confira o resultado do seu treino:
        </p>

        <div class="resumo-metricas">
          <div class="metrica-item">
            <span class="metrica-rotulo">Respondidas</span>
            <strong class="metrica-valor">{{ state.sessaoResumo().questoesRespondidas }}</strong>
          </div>
          <div class="metrica-item metrica-item--acerto">
            <span class="metrica-rotulo">Acertos</span>
            <strong class="metrica-valor">{{ state.sessaoResumo().acertos }} ({{ taxaAcerto() }}%)</strong>
          </div>
          <div class="metrica-item metrica-item--xp">
            <span class="metrica-rotulo">XP Ganho</span>
            <strong class="metrica-valor">+{{ state.sessaoResumo().xpTotalGanho }} XP</strong>
          </div>
        </div>

        <div class="resumo-acoes">
          <button
            type="button"
            class="btn-resumo btn-resumo--primario"
            (click)="state.voltarParaListagem()"
          >
            <i class="pi pi-list" aria-hidden="true"></i>
            <span>Voltar para a Listagem</span>
          </button>
          <a routerLink="/desempenho" class="btn-resumo btn-resumo--secundario">
            <i class="pi pi-chart-pie" aria-hidden="true"></i>
            <span>Ver Meu Desempenho</span>
          </a>
        </div>
      </article>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .resumo-wrapper {
      display: flex;
      justify-content: center;
      padding: 2rem 0;
    }

    .resumo-card {
      width: min(100%, 34rem);
      padding: clamp(1.75rem, 4vw, 2.5rem);
      background: #fff;
      border: 1px solid #e5edef;
      border-radius: 1.25rem;
      box-shadow: 0 0.7rem 2rem rgba(16, 42, 58, 0.05);
      text-align: center;
    }

    .resumo-icone {
      display: grid;
      width: 4.5rem;
      height: 4.5rem;
      margin: 0 auto 1.25rem;
      place-items: center;
      border-radius: 50%;
      background: #ddfae9;
      color: #19aa62;
      font-size: 2rem;
    }

    .resumo-titulo {
      margin: 0 0 0.5rem;
      color: #102e45;
      font-size: clamp(1.5rem, 3vw, 1.85rem);
      font-weight: 800;
      letter-spacing: -0.04em;
    }

    .resumo-subtitulo {
      margin: 0 0 1.75rem;
      color: #667d8c;
      font-size: 0.95rem;
      line-height: 1.5;
    }

    .resumo-metricas {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0.75rem;
      margin-bottom: 1.75rem;
    }

    .metrica-item {
      display: grid;
      gap: 0.35rem;
      padding: 1rem 0.75rem;
      border: 1px solid #edf1f3;
      border-radius: 0.85rem;
      background: #f8fafb;
    }

    .metrica-rotulo {
      color: #667d8c;
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .metrica-valor {
      color: #102e45;
      font-size: 1.15rem;
      font-weight: 800;
    }

    .metrica-item--acerto .metrica-valor {
      color: #16a34a;
    }

    .metrica-item--xp .metrica-valor {
      color: #f76c14;
    }

    .resumo-acoes {
      display: grid;
      gap: 0.75rem;
    }

    .btn-resumo {
      display: inline-flex;
      min-height: 3rem;
      width: 100%;
      align-items: center;
      justify-content: center;
      gap: 0.6rem;
      padding: 0.75rem 1.25rem;
      border-radius: 0.75rem;
      font-size: 0.95rem;
      font-weight: 800;
      cursor: pointer;
      transition: all 0.15s ease-in-out;
    }

    .btn-resumo--primario {
      border: 0;
      background: #006494;
      color: #fff;
    }

    .btn-resumo--primario:hover {
      background: #03293d;
    }

    .btn-resumo--secundario {
      border: 1px solid #d4e0e5;
      background: #fff;
      color: #102e45;
    }

    .btn-resumo--secundario:hover {
      border-color: #a8d4df;
      background: #f3fbfd;
      color: #006494;
    }

    @media (max-width: 480px) {
      .resumo-metricas {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class EstudarResumoComponent {
  readonly state = inject(EstudarStateService);

  protected readonly taxaAcerto = computed(() => {
    const { questoesRespondidas, acertos } = this.state.sessaoResumo();
    if (questoesRespondidas === 0) return 0;
    return Math.round((acertos / questoesRespondidas) * 100);
  });
}
