import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { SessaoService } from './sessao.service';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);

  const requisicaoClonada = req.clone({
    withCredentials: true
  });

  return next(requisicaoClonada).pipe(
    catchError((erro) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401) {
        if (!req.url.includes('/login') && !req.url.includes('/refresh-token')) {
          sessao.limparSessaoLocal();
          void router.navigate(['/login']);
        }
      }
      return throwError(() => erro);
    })
  );
};
