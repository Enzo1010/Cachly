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
    // Prepara o state mockado com uma questao para satisfazer a checagem questaoAtiva() != null
    service.questoes.set([{
      id: 99,
      enunciado: 'Questão Teste',
      dificuldade: 'FACIL' as DificuldadeQuestao,
      xpBase: 50,
      alternativas: []
    }]);
    service.iniciarEstudo();

    // Tenta submeter uma resposta
    service.responder(1);

    // Intercepta a requisição na API
    const req = httpMock.expectOne('/api/questoes/99/respostas');
    expect(req.request.method).toBe('POST');

    // Emula a resposta de erro customizada do backend (ApiExceptionHandler)
    const erroBackend = {
      erro: 'Bad Request',
      mensagem: 'Existem campos inválidos na requisição',
      campos: {
        'alternativaId': 'O ID da alternativa não existe',
        'regraNegocio': 'Usuário sem permissão para essa categoria'
      }
    };

    req.flush(erroBackend, { status: 400, statusText: 'Bad Request' });

    // Avalia se o Signal de erro foi atualizado desempacotando as mensagens do dict "campos",
    // em vez de usar a mensagem global ofuscada
    expect(service.error()).toBe('Erro de Validação: O ID da alternativa não existe | Usuário sem permissão para essa categoria');
    expect(service.respondendo()).toBe(false);
  });

  it('deve processar resposta com sucesso e atualizar a sessão com nomeNivel correto (não undefined)', () => {
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
      xpTotal: 110
    };

    req.flush(respostaMock);

    expect(service.resultadoResposta()).toEqual(respostaMock);
    expect(service.resultadoResposta()?.nomeNivel).toBe('Dev Junior');
    expect(spyAtualizar).toHaveBeenCalledTimes(1);
    expect(spyAtualizar).toHaveBeenCalledWith(110, 2, 'Dev Junior');
    expect(spyAtualizar).not.toHaveBeenCalledWith(expect.anything(), expect.anything(), undefined);
    expect(service.respondendo()).toBe(false);
    expect(service.sessaoResumo()).toEqual({
      questoesRespondidas: 1,
      acertos: 1,
      xpTotalGanho: 10
    });
  });
});