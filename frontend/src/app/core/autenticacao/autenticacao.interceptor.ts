import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { SessaoService } from './sessao.service';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const requisicaoClonada = req.clone({
    withCredentials: true
  });

  return next(requisicaoClonada);
};
