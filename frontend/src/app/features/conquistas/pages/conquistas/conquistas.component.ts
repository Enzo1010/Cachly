import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { ConquistasService } from '../../services/conquistas.service';

@Component({
  selector: 'app-conquistas',
  templateUrl: './conquistas.component.html',
  styleUrl: './conquistas.component.scss',
})
export class ConquistasComponent {
  private readonly conquistasService = inject(ConquistasService);

  protected readonly conquistas = toSignal(this.conquistasService.obterConquistas(), {
    initialValue: [],
  });
}
