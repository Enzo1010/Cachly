import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { MessageService } from 'primeng/api';
import { firstValueFrom } from 'rxjs';

import { TEMA_CACHLY } from './core/config/tema-cachly';
import { autenticacaoInterceptor } from './core/autenticacao/autenticacao.interceptor';
import { routes } from './app.routes';
import { SessaoService } from './core/autenticacao/sessao.service';

function initializeApp(sessaoService: SessaoService) {
  return () => firstValueFrom(sessaoService.carregarSessao());
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withInterceptors([autenticacaoInterceptor]),
      // Lê o cookie XSRF-TOKEN gerado pelo Spring e o reenvia no header
      // X-XSRF-TOKEN em todo método que altera estado (POST, PUT, PATCH, DELETE).
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
    ),
    provideRouter(routes),
    MessageService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [SessaoService],
      multi: true
    },
    providePrimeNG({
      ripple: true,
      theme: {
        preset: TEMA_CACHLY,
        options: {
          darkModeSelector: false,
        },
      },
    }),
  ],
};

