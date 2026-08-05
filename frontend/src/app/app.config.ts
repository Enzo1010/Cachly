import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { MessageService } from 'primeng/api';

import { TEMA_QUESTLY } from './core/config/tema-questly';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(routes),
    MessageService,
    providePrimeNG({
      ripple: true,
      theme: {
        preset: TEMA_QUESTLY,
        options: {
          darkModeSelector: false,
        },
      },
    }),
  ],
};
