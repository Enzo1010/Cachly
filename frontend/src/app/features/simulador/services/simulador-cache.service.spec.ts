import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SimuladorCacheService } from './simulador-cache.service';
import { SimulacaoRequest } from '../models/simulador.model';

describe('SimuladorCacheService', () => {
  let service: SimuladorCacheService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SimuladorCacheService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(SimuladorCacheService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve fazer o post com a requisicao de simulacao', () => {
    const mockRequest: SimulacaoRequest = {
      tamanhoCacheBytes: 16,
      tamanhoBlocoBytes: 4,
      numeroVias: null,
      mapeamento: 'DIRETO',
      substituicao: null,
      enderecos: [0, 4, 8]
    };

    service.executarSimulacao(mockRequest).subscribe((response) => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne('/api/simulador/executar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush({});
  });

  it('deve listar desafios com chamada GET', () => {
    service.listarDesafios().subscribe((desafios) => {
      expect(desafios.length).toBe(1);
      expect(desafios[0].id).toBe('cold-miss');
    });

    const req = httpMock.expectOne('/api/simulador/desafios');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 'cold-miss', titulo: 'Miss Compulsorio' }]);
  });

  it('deve verificar desafio com chamada POST', () => {
    service.verificarDesafio('cold-miss', { opcaoSelecionadaId: 'A' }).subscribe((res) => {
      expect(res.correto).toBe(true);
      expect(res.xpGanho).toBe(30);
    });

    const req = httpMock.expectOne('/api/simulador/desafios/cold-miss/verificar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ opcaoSelecionadaId: 'A' });
    req.flush({ correto: true, xpGanho: 30 });
  });
});
