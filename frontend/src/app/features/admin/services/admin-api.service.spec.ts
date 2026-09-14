import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AdminApiService } from './admin-api.service';
import { QuestaoAdminRequest, CategoriaAdminRequest } from '../models/admin.model';

describe('AdminApiService', () => {
  let service: AdminApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AdminApiService],
    });

    service = TestBed.inject(AdminApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('deve listar todas as questoes para o admin', () => {
    service.listarQuestoes().subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].id).toBe(10);
    });

    const req = http.expectOne('/api/questoes/admin/todas');
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: 10,
        categoriaId: 1,
        categoriaNome: 'Cache',
        enunciado: 'O que é miss?',
        explicacao: 'Falta do bloco',
        dificuldade: 'FACIL',
        xpBase: 10,
        ativa: true,
        alternativas: [],
      },
    ]);
  });

  it('deve cadastrar nova questao', () => {
    const payload: QuestaoAdminRequest = {
      categoriaId: 1,
      enunciado: 'Pergunta',
      explicacao: 'Explicacao',
      dificuldade: 'FACIL',
      xpBase: 10,
      alternativas: [
        { texto: 'A', correta: true, ordem: 1 },
        { texto: 'B', correta: false, ordem: 2 },
      ],
    };

    service.cadastrarQuestao(payload).subscribe((res) => {
      expect(res.id).toBe(1);
    });

    const req = http.expectOne('/api/questoes');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 1, ...payload, categoriaNome: 'Cat', ativa: true });
  });

  it('deve ativar e desativar questao', () => {
    service.ativarQuestao(5).subscribe((res) => expect(res.ativa).toBe(true));
    const reqAtivar = http.expectOne('/api/questoes/5/ativar');
    expect(reqAtivar.request.method).toBe('PATCH');
    reqAtivar.flush({ id: 5, ativa: true });

    service.desativarQuestao(5).subscribe((res) => expect(res.ativa).toBe(false));
    const reqDesativar = http.expectOne('/api/questoes/5/desativar');
    expect(reqDesativar.request.method).toBe('PATCH');
    reqDesativar.flush({ id: 5, ativa: false });
  });

  it('deve listar todas as categorias para o admin', () => {
    service.listarCategorias().subscribe((res) => {
      expect(res.length).toBe(2);
    });

    const req = http.expectOne('/api/categorias/admin/todas');
    expect(req.request.method).toBe('GET');
    req.flush([
      { id: 1, nome: 'Cat 1', ativa: true },
      { id: 2, nome: 'Cat 2', ativa: false },
    ]);
  });

  it('deve cadastrar nova categoria', () => {
    const payload: CategoriaAdminRequest = { nome: 'Nova Cat', descricao: 'Desc' };

    service.cadastrarCategoria(payload).subscribe((res) => {
      expect(res.id).toBe(1);
    });

    const req = http.expectOne('/api/categorias');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, ...payload, ativa: true });
  });
});
