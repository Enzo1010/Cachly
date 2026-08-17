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
});
