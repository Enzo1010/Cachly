import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { EstudarApiService } from '../../services/estudar-api.service';
import { EstudarPageComponent } from './estudar-page.component';

describe('EstudarPageComponent', () => {
  async function montar(categoriaId: string | null) {
    const listarQuestoes = vi.fn().mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [EstudarPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(categoriaId === null ? {} : { categoriaId }),
            },
          },
        },
        { provide: SessaoService, useValue: {} },
        {
          provide: EstudarApiService,
          useValue: { listarCategorias: () => of([]), listarQuestoes },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(EstudarPageComponent);
    fixture.detectChanges();
    return { fixture, listarQuestoes };
  }

  it('abre a categoria indicada pela tela de desempenho', async () => {
    const { fixture, listarQuestoes } = await montar('7');

    expect(listarQuestoes).toHaveBeenCalledWith(7);
    expect(fixture.componentInstance.state.categoriaFiltro()).toBe(7);
  });

  it('ignora um identificador de categoria inválido', async () => {
    const { fixture, listarQuestoes } = await montar('invalido');

    expect(listarQuestoes).toHaveBeenCalledWith(undefined);
    expect(fixture.componentInstance.state.categoriaFiltro()).toBeNull();
  });
});
