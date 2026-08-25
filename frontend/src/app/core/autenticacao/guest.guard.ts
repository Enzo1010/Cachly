import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessaoService } from './sessao.service';

export const guestGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);

  return sessao.estaAutenticado() ? router.createUrlTree(['/dashboard']) : true;
};
