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
        const ROTAS_ONDE_401_NAO_DESLOGA = [
          '/api/auth/login',
          '/api/auth/alterar-senha'
        ];

        // Diferencia pela origem da requisição:
        // Se a rota NÃO estiver na lista de exceções, o 401 indica
        // sessão expirada ou token inválido -> deve deslogar.
        const deveDeslogar = !ROTAS_ONDE_401_NAO_DESLOGA.some(rota => req.url.includes(rota));

        if (deveDeslogar) {
          sessao.limparSessaoLocal();
          void router.navigate(['/login']);
        }
      }
      return throwError(() => erro);
    })
  );
};
