import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessaoService } from './sessao.service';

export const adminGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);

  if (!sessao.estaAutenticado()) {
    return router.createUrlTree(['/login']);
  }

  if (sessao.usuario()?.perfil !== 'ADMINISTRADOR') {
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
