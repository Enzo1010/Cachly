import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('should display the dashboard summary', () => {
    const conteudo = fixture.nativeElement as HTMLElement;

    expect(conteudo.querySelector('h2')?.textContent).toContain('Olá, Luis!');
    expect(conteudo.querySelectorAll('.indicador')).toHaveLength(3);
    expect(conteudo.textContent).toContain('Seu próximo objetivo');
  });
});
