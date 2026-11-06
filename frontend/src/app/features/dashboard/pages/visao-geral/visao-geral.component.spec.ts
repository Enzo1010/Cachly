import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { SessaoService } from '../../../../core/autenticacao/sessao.service';
import { DesempenhoResponse } from '../../../../shared/models/desempenho.model';
import { DesempenhoApiService } from '../../../../shared/services/desempenho-api.service';
import { VisaoGeralComponent } from './visao-geral.component';

describe('VisaoGeralComponent', () => {
  let fixture: ComponentFixture<VisaoGeralComponent>;

  async function montar(desempenho: DesempenhoResponse): Promise<HTMLElement> {
    await TestBed.configureTestingModule({
      imports: [VisaoGeralComponent],
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: {
            usuario: signal({
              id: 1,
              nome: 'Ana Silva',
              email: 'ana.silva@exemplo.com',
              perfil: 'ALUNO',
              xpTotal: 0,
              nivel: 1,
              diasOfensiva: 12,
              token: 'token-de-teste',
            }),
          },
        },
        {
          provide: DesempenhoApiService,
          useValue: {
            obterDesempenho: () => of(desempenho),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VisaoGeralComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('deve exibir o resumo da visao geral', async () => {
    const conteudo = await montar({
      totalTentativas: 10,
      acertos: 8,
      taxaAcerto: 80,
      estatisticasPorCategoria: [],
    });

    expect(conteudo.querySelector('h2')?.textContent).toContain('Olá, Ana Silva!');
    expect(conteudo.querySelectorAll('.indicador')).toHaveLength(3);
    expect(conteudo.textContent).toContain('Sua recomendação');
  });

  it('nao recomenda revisao quando a categoria tem menos de 3 tentativas ou 100% de acerto', async () => {
    const conteudo = await montar({
      totalTentativas: 4,
      acertos: 4,
      taxaAcerto: 100,
      estatisticasPorCategoria: [
        {
          categoriaId: 1,
          categoriaNome: 'Sistemas de Numeração',
          totalTentativas: 3,
          acertos: 3,
          taxaAcerto: 100,
        },
        {
          categoriaId: 2,
          categoriaNome: 'Portas Lógicas',
          totalTentativas: 1,
          acertos: 0,
          taxaAcerto: 0,
        },
      ],
    });

    expect(conteudo.querySelector('.objetivo__texto')?.textContent).toContain(
      'Continue respondendo questões',
    );
    expect(conteudo.querySelector('.botao-desafio')?.textContent).toContain('Responder Questões');
  });

  it('recomenda a categoria com menor taxa de acerto quando houver pelo menos 3 tentativas e erros', async () => {
    const conteudo = await montar({
      totalTentativas: 8,
      acertos: 4,
      taxaAcerto: 50,
      estatisticasPorCategoria: [
        {
          categoriaId: 1,
          categoriaNome: 'Memória Cache',
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
    });

    expect(conteudo.querySelector('.objetivo__texto')?.textContent).toContain('Portas Lógicas');
    expect(conteudo.querySelector('.objetivo__texto')?.textContent).toContain('2 de 5 tentativas');
    expect(conteudo.querySelector('.botao-desafio')?.textContent).toContain(
      'Praticar Portas Lógicas',
    );
  });
});
