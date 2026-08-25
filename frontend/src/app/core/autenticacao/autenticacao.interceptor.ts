import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { SessaoService } from './sessao.service';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const sessaoService = inject(SessaoService);
  const token = sessaoService.usuario()?.token;

  let requisicaoClonada = req.clone({
    withCredentials: true
  });

  if (token) {
    requisicaoClonada = requisicaoClonada.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(requisicaoClonada);
};
