import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { ComentarioApiService, ComentarioResponse } from '../../services/comentario-api.service';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-forum-questao',
  standalone: true,
  imports: [ButtonModule, TextareaModule, FormsModule, DatePipe],
  template: `
    <div class="forum-container">
      <h3 class="forum-title">Fórum e Comentários</h3>
      
      <div class="novo-comentario">
        <textarea 
          pInputTextarea 
          [(ngModel)]="novoComentario" 
          rows="3" 
          placeholder="Adicione um comentário..."
          class="w-full mb-2">
        </textarea>
        <div style="display: flex; justify-content: flex-end;">
          <p-button 
            label="Comentar" 
            icon="pi pi-send" 
            (onClick)="adicionarComentario()" 
            [disabled]="!novoComentario.trim()"
            [loading]="enviando()">
          </p-button>
        </div>
      </div>

      <div class="comentarios-lista">
        @for (comentario of comentarios(); track comentario.id) {
          <div class="comentario-item">
            <div class="upvote-col">
              <button 
                class="upvote-btn" 
                [class.ativo]="comentario.usuarioDeuUpvote"
                (click)="darUpvote(comentario.id)">
                <i class="pi" [class.pi-thumbs-up-fill]="comentario.usuarioDeuUpvote" [class.pi-thumbs-up]="!comentario.usuarioDeuUpvote"></i>
              </button>
              <span class="upvote-count">{{ comentario.upvotes }}</span>
            </div>
            
            <div class="comentario-conteudo">
              <div class="comentario-header">
                <span class="autor-nome">{{ comentario.autorNome }}</span>
                @if (comentario.autorPerfil === 'ADMINISTRADOR') {
                  <span class="badge-admin">Administrador</span>
                }
                <span class="comentario-data">{{ comentario.criadoEm | date:'short' }}</span>
              </div>
              <p class="comentario-texto">{{ comentario.conteudo }}</p>
            </div>
          </div>
        } @empty {
          <p class="sem-comentarios">Nenhum comentário ainda. Seja o primeiro a comentar!</p>
        }
      </div>
    </div>
  `,
  styleUrls: ['./forum-questao.component.scss']
})
export class ForumQuestaoComponent implements OnInit {
  @Input({ required: true }) questaoId!: number;
  
  private readonly comentarioApi = inject(ComentarioApiService);
  
  comentarios = signal<ComentarioResponse[]>([]);
  novoComentario = '';
  enviando = signal(false);

  ngOnInit() {
    this.carregarComentarios();
  }

  carregarComentarios() {
    this.comentarioApi.listarPorQuestao(this.questaoId).subscribe({
      next: (comentarios) => this.comentarios.set(comentarios),
      error: (err) => console.error('Erro ao carregar comentarios', err)
    });
  }

  adicionarComentario() {
    if (!this.novoComentario.trim()) return;
    
    this.enviando.set(true);
    this.comentarioApi.adicionarComentario(this.questaoId, this.novoComentario).subscribe({
      next: (comentario) => {
        this.comentarios.update(comentarios => [comentario, ...comentarios]);
        this.novoComentario = '';
        this.enviando.set(false);
      },
      error: (err) => {
        console.error('Erro ao adicionar comentario', err);
        this.enviando.set(false);
      }
    });
  }

  darUpvote(comentarioId: number) {
    this.comentarioApi.darUpvote(comentarioId).subscribe({
      next: () => {
        this.comentarios.update(comentarios => 
          comentarios.map(c => {
            if (c.id === comentarioId) {
              const mudanca = c.usuarioDeuUpvote ? -1 : 1;
              return { 
                ...c, 
                upvotes: c.upvotes + mudanca,
                usuarioDeuUpvote: !c.usuarioDeuUpvote 
              };
            }
            return c;
          })
        );
      },
      error: (err) => console.error('Erro ao dar upvote', err)
    });
  }
}
