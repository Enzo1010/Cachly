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
        // Só redireciona para login em chamadas de sessão (GET /api/auth/me)
        // ou quando o SecurityFilter rejeita o token (resposta sem corpo JSON do app).
        // Não redireciona em erros 401 de endpoints de negócio para não mascarar bugs.
        const isRotaDeLogin = req.url.includes('/login') || req.url.includes('/refresh-token');
        const isChecagemDeSessao = req.url.includes('/api/auth/me');
        const isRespostaDoSpring = !erro.error?.timestamp; // ErroResponse do app tem timestamp

        if (!isRotaDeLogin && (isChecagemDeSessao || isRespostaDoSpring)) {
          sessao.limparSessaoLocal();
          void router.navigate(['/login']);
        }
      }
      return throwError(() => erro);
    })
  );
};
