import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { DesempenhoResponse } from '../../../../shared/models/desempenho.model';
import { DesempenhoApiService } from '../../../../shared/services/desempenho-api.service';
import { DesempenhoComponent } from './desempenho.component';

describe('DesempenhoComponent', () => {
  let fixture: ComponentFixture<DesempenhoComponent>;

  async function montar(
    obterDesempenho: () => Observable<DesempenhoResponse>,
  ): Promise<HTMLElement> {
    await TestBed.configureTestingModule({
      imports: [DesempenhoComponent],
      providers: [
        provideRouter([]),
        { provide: DesempenhoApiService, useValue: { obterDesempenho } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DesempenhoComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('orienta o aluno sem tentativas a começar o estudo', async () => {
    const resposta = new Subject<DesempenhoResponse>();
    const tela = await montar(() => resposta.asObservable());

    expect(tela.textContent).toContain('Carregando seu desempenho');

    resposta.next({ totalTentativas: 0, acertos: 0, taxaAcerto: 0, estatisticasPorCategoria: [] });
    fixture.detectChanges();

    expect(tela.textContent).toContain('Comece por uma categoria');
    expect(tela.querySelector('.proximo-passo__acao')?.getAttribute('href')).toBe('/estudar');
    expect(tela.querySelector('.categorias')).toBeNull();
  });

  it('recomenda a categoria com menor taxa de acerto e abre o estudo filtrado', async () => {
    const tela = await montar(() =>
      of({
        totalTentativas: 8,
        acertos: 4,
        taxaAcerto: 50,
        estatisticasPorCategoria: [
          {
            categoriaId: 1,
            categoriaNome: 'Memória',
            totalTentativas: 3,
            acertos: 2,
            taxaAcerto: 67,
          },
          {
            categoriaId: 2,
            categoriaNome: 'Portas Lógicas',
            totalTentativas: 5,
            acertos: 2,
            taxaAcerto: 40,
          },
        ],
      }),
    );

    expect(tela.querySelector('#titulo-proximo-passo')?.textContent).toContain('Portas Lógicas');
    expect(tela.textContent).toContain('2 de 5');
    expect(tela.querySelector('.proximo-passo__acao')?.getAttribute('href')).toBe(
      '/estudar?categoriaId=2',
    );
    expect(tela.querySelectorAll('.categoria')).toHaveLength(2);
    expect(tela.textContent).not.toContain('Progresso da Trilha');
  });

  it('evita apontar uma fraqueza com apenas uma tentativa', async () => {
    const tela = await montar(() =>
      of({
        totalTentativas: 1,
        acertos: 0,
        taxaAcerto: 0,
        estatisticasPorCategoria: [
          { categoriaId: 3, categoriaNome: 'CPU', totalTentativas: 1, acertos: 0, taxaAcerto: 0 },
        ],
      }),
    );

    expect(tela.querySelector('#titulo-proximo-passo')?.textContent).toContain(
      'Continue explorando os temas',
    );
    expect(tela.textContent).not.toContain('Revise CPU');
  });

  it('mostra erro e permite tentar novamente', async () => {
    const obterDesempenho = vi
      .fn()
      .mockReturnValueOnce(throwError(() => new Error('Falha de rede')))
      .mockReturnValueOnce(
        of({
          totalTentativas: 0,
          acertos: 0,
          taxaAcerto: 0,
          estatisticasPorCategoria: [],
        }),
      );
    const tela = await montar(obterDesempenho);

    expect(tela.textContent).toContain('Não foi possível carregar');
    (tela.querySelector('.estado button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(obterDesempenho).toHaveBeenCalledTimes(2);
    expect(tela.textContent).toContain('Comece por uma categoria');
  });
});
