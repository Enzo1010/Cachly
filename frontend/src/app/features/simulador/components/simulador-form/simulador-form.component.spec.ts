import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimuladorFormComponent } from './simulador-form.component';
import { MessageService } from 'primeng/api';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('SimuladorFormComponent', () => {
  let component: SimuladorFormComponent;
  let fixture: ComponentFixture<SimuladorFormComponent>;
  let messageServiceMock: { add: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    messageServiceMock = { add: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [SimuladorFormComponent],
      providers: [
        { provide: MessageService, useValue: messageServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SimuladorFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve ser instanciado com valores padrão válidos', () => {
    expect(component).toBeTruthy();
    expect(component.form.controls.enderecosStr.value).toBe('0, 4, 8, 12, 0, 4');
  });

  it('deve carregar um preset preenchendo o controle de endereços e emitindo notificação', () => {
    const preset = component.presets[0];
    component.carregarPreset(preset.enderecos);

    expect(component.form.controls.enderecosStr.value).toBe(preset.enderecos);
    expect(messageServiceMock.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'info',
        summary: 'Preset Aplicado'
      })
    );
  });

  it('deve gerar endereços aleatórios dentro do intervalo e quantidade solicitados', () => {
    component.gerarEnderecosAleatorios(5, 32);

    const valor = component.form.controls.enderecosStr.value;
    const lista = valor.split(',').map(s => parseInt(s.trim(), 10));

    expect(lista.length).toBe(5);
    lista.forEach(num => {
      expect(num).toBeGreaterThanOrEqual(0);
      expect(num).toBeLessThan(32);
    });
    expect(messageServiceMock.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'success',
        summary: 'Endereços Aleatórios Gerados'
      })
    );
  });
});
