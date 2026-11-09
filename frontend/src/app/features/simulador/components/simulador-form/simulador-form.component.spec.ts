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

  it('deve exigir número de vias e política de substituição no mapeamento associativo por conjunto', () => {
    const emitSpy = vi.spyOn(component.simular, 'emit');

    component.form.controls.mapeamento.setValue('CONJUNTO_ASSOCIATIVO');
    expect(component.form.controls.numeroVias.value).toBe(2);
    expect(component.form.valid).toBe(true);

    component.form.controls.numeroVias.setValue(null);
    expect(component.form.invalid).toBe(true);

    component.onSubmit();
    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('deve impedir simulação quando número de vias excede o total de linhas da cache', () => {
    const emitSpy = vi.spyOn(component.simular, 'emit');

    component.form.controls.tamanhoCacheBytes.setValue(32);
    component.form.controls.tamanhoBlocoBytes.setValue(8); // 4 linhas no total
    component.form.controls.mapeamento.setValue('CONJUNTO_ASSOCIATIVO');
    component.form.controls.numeroVias.setValue(8); // 8 vias > 4 linhas

    component.onSubmit();

    expect(emitSpy).not.toHaveBeenCalled();
    expect(messageServiceMock.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        summary: 'Erro de Configuração'
      })
    );
  });
});
