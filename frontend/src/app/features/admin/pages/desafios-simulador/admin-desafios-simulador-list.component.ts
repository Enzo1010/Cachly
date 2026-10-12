import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AdminDesafioSimuladorApiService } from '../../services/admin-desafio-simulador-api.service';
import { DesafioCacheResponse } from '../../models/admin-desafio.model';

@Component({
  selector: 'app-admin-desafios-simulador-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TableModule, ButtonModule, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './admin-desafios-simulador-list.component.html',
  styleUrls: ['./admin-desafios-simulador-list.component.scss']
})
export class AdminDesafiosSimuladorListComponent implements OnInit {
  private readonly adminDesafioService = inject(AdminDesafioSimuladorApiService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly router = inject(Router);

  desafios = signal<DesafioCacheResponse[]>([]);
  carregando = signal<boolean>(true);

  ngOnInit(): void {
    this.carregarDesafios();
  }

  carregarDesafios(): void {
    this.carregando.set(true);
    this.adminDesafioService.listarTodos().subscribe({
      next: (dados) => {
        this.desafios.set(dados);
        this.carregando.set(false);
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar os desafios.' });
        this.carregando.set(false);
      }
    });
  }

  novoDesafio(): void {
    this.router.navigate(['/admin/desafios/novo']);
  }

  editarDesafio(id: string): void {
    this.router.navigate(['/admin/desafios/editar', id]);
  }

  excluirDesafio(desafio: DesafioCacheResponse): void {
    this.confirmationService.confirm({
      message: `Tem certeza que deseja excluir o desafio "${desafio.titulo}"?`,
      header: 'Confirmar Exclusão',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sim',
      rejectLabel: 'Não',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.adminDesafioService.excluir(desafio.id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Sucesso', detail: 'Desafio excluído com sucesso.' });
            this.carregarDesafios();
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: 'Erro', detail: 'Falha ao excluir o desafio.' });
          }
        });
      }
    });
  }
}
