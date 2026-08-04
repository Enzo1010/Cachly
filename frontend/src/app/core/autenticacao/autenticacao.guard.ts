import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessaoService } from './sessao.service';

export const autenticacaoGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);

  return sessao.estaAutenticado() ? true : router.createUrlTree(['/login']);
};
