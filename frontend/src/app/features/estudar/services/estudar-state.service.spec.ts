import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { EstudarStateService } from './estudar-state.service';
import { EstudarApiService } from './estudar-api.service';
import { SessaoService } from '../../../core/autenticacao/sessao.service';
import { DificuldadeQuestao } from '../models/estudar.model';

describe('EstudarStateService', () => {
  let service: EstudarStateService;
  let httpMock: HttpTestingController;
  let sessaoService: SessaoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        EstudarStateService,
        EstudarApiService,
        SessaoService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(EstudarStateService);
    httpMock = TestBed.inject(HttpTestingController);
    sessaoService = TestBed.inject(SessaoService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve formatar mensagens especificas de erro quando a API retornar 400 Bad Request com campos', () => {
    service.questoes.set([{
      id: 99,
      enunciado: 'Questão Teste',
      dificuldade: 'FACIL' as DificuldadeQuestao,
      xpBase: 50,
      alternativas: []
    }]);
    service.iniciarEstudo();

    service.responder(1);

    const req = httpMock.expectOne('/api/questoes/99/respostas');
    expect(req.request.method).toBe('POST');

    const erroBackend = {
      erro: 'Bad Request',
      mensagem: 'Existem campos inválidos na requisição',
      campos: {
        'alternativaId': 'O ID da alternativa não existe',
        'regraNegocio': 'Usuário sem permissão para essa categoria'
      }
    };

    req.flush(erroBackend, { status: 400, statusText: 'Bad Request' });

    expect(service.error()).toBe('Erro de Validação: O ID da alternativa não existe | Usuário sem permissão para essa categoria');
    expect(service.respondendo()).toBe(false);
  });

  it('deve processar resposta com sucesso e atualizar a sessão com nomeNivel e diasOfensiva', () => {
    const spyAtualizar = vi.spyOn(sessaoService, 'atualizarAposResposta');

    service.questoes.set([{
      id: 42,
      enunciado: 'Questão Teste',
      dificuldade: 'FACIL' as DificuldadeQuestao,
      xpBase: 10,
      alternativas: []
    }]);
    service.iniciarEstudo();

    service.responder(1);

    const req = httpMock.expectOne('/api/questoes/42/respostas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ alternativaId: 1 });

    const respostaMock = {
      tentativaId: 101,
      correta: true,
      alternativaCorretaId: 1,
      explicacao: 'A resposta está correta.',
      xpConcedido: 10,
      nivelAtual: 2,
      nomeNivel: 'Dev Junior',
      xpTotal: 110,
      diasOfensiva: 3
    };

    req.flush(respostaMock);

    expect(service.resultadoResposta()).toEqual(respostaMock);
    expect(service.resultadoResposta()?.nomeNivel).toBe('Dev Junior');
    expect(spyAtualizar).toHaveBeenCalledTimes(1);
    expect(spyAtualizar).toHaveBeenCalledWith(110, 2, 'Dev Junior', 3);
    expect(spyAtualizar).not.toHaveBeenCalledWith(expect.anything(), expect.anything(), undefined);
    expect(service.respondendo()).toBe(false);
    expect(service.sessaoResumo()).toEqual({
      questoesRespondidas: 1,
      acertos: 1,
      xpTotalGanho: 10
    });
  });

  it('deve iniciar o estudo diretamente no indice da questao selecionada', () => {
    service.questoes.set([
      { id: 1, enunciado: 'Q1', dificuldade: 'FACIL', xpBase: 10, alternativas: [] },
      { id: 2, enunciado: 'Q2', dificuldade: 'MEDIA', xpBase: 20, alternativas: [] },
      { id: 3, enunciado: 'Q3', dificuldade: 'DIFICIL', xpBase: 30, alternativas: [] }
    ]);

    service.iniciarEstudo(2);

    expect(service.telaAtual()).toBe('RESOLUCAO');
    expect(service.questaoAtualIndex()).toBe(2);
    expect(service.questaoAtiva()?.id).toBe(3);
  });
});
