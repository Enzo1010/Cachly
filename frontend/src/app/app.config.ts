import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { MessageService } from 'primeng/api';

import { TEMA_CACHLY } from './core/config/tema-cachly';
import { autenticacaoInterceptor } from './core/autenticacao/autenticacao.interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([autenticacaoInterceptor])),
    provideRouter(routes),
    MessageService,
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
