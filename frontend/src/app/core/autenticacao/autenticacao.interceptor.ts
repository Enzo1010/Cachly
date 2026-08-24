import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { SessaoService } from './sessao.service';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const sessaoService = inject(SessaoService);
  const token = sessaoService.usuario()?.token;

  if (token) {
    const requisicaoClonada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });

    return next(requisicaoClonada);
  }

  return next(req);
};
