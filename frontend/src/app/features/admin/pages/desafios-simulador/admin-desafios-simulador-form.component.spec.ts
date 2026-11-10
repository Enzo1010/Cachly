import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AdminDesafiosSimuladorFormComponent } from './admin-desafios-simulador-form.component';
import { AdminDesafioSimuladorApiService } from '../../services/admin-desafio-simulador-api.service';
import { DesafioCacheResponse } from '../../models/admin-desafio.model';

describe('Formulário administrativo de desafios', () => {
  const desafio: DesafioCacheResponse = {
    id: 'cache-basica', titulo: 'Cache básica', descricao: 'Descrição', dificuldade: 'FACIL',
    xpRecompensa: 30, pergunta: 'Quantos misses?', dica: 'Cache vazia', explicacao: 'Primeiro acesso',
    opcaoCorretaId: 'A', opcoes: [{ id: 'A', texto: 'Um' }, { id: 'B', texto: 'Dois' }],
    configuracao: { tamanhoCacheBytes: 16, tamanhoBlocoBytes: 4, numeroVias: null,
      mapeamento: 'DIRETO', substituicao: null, enderecos: [0] }
  };
  let id: string | null;
  let api: { obterPorId: ReturnType<typeof vi.fn>; criar: ReturnType<typeof vi.fn>; atualizar: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    id = null;
    api = { obterPorId: vi.fn(() => of(desafio)), criar: vi.fn(() => new Subject()), atualizar: vi.fn(() => new Subject()) };
    TestBed.configureTestingModule({
      imports: [AdminDesafiosSimuladorFormComponent],
      providers: [provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => id } } } },
        { provide: AdminDesafioSimuladorApiService, useValue: api }]
    });
  });

  it('deve enviar o identificador preenchido no cadastro', () => {
    const fixture = TestBed.createComponent(AdminDesafiosSimuladorFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.patchValue({ ...desafio, configuracao: { ...desafio.configuracao, enderecosTexto: '0' } });
    component.removerOpcao(3);
    component.removerOpcao(2);
    component.salvar();
    expect(api.criar).toHaveBeenCalledWith(expect.objectContaining({ id: 'cache-basica', opcaoCorretaId: 'A', explicacao: 'Primeiro acesso' }));
  });

  it('deve carregar e preservar gabarito e explicação ao editar somente o título', () => {
    id = desafio.id;
    const fixture = TestBed.createComponent(AdminDesafiosSimuladorFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.form.valid).toBe(true);
    component.form.controls['titulo'].setValue('Título atualizado');
    component.salvar();
    expect(api.atualizar).toHaveBeenCalledWith(desafio.id, expect.objectContaining({
      titulo: 'Título atualizado', opcaoCorretaId: 'A', explicacao: 'Primeiro acesso',
      configuracao: expect.objectContaining({ substituicao: null })
    }));
  });
});
