import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';

import { PerfilApiService } from './perfil-api.service';
import { AlterarSenhaRequest } from '../models/perfil.model';

describe('PerfilApiService', () => {
  let service: PerfilApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PerfilApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(PerfilApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve enviar requisição POST para /api/auth/alterar-senha com payload correto', () => {
    const payload: AlterarSenhaRequest = {
      senhaAtual: 'senhaAtual123',
      novaSenha: 'novaSenha456',
    };

    service.alterarSenha(payload).subscribe();

    const req = httpMock.expectOne('/api/auth/alterar-senha');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(null);
  });
});
