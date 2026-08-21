import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { Conquista } from '../models/conquista.model';

// TODO: substituir pelos dados mockados abaixo por uma chamada HTTP real
// (ex.: GET /api/alunos/me/conquistas)
const CONQUISTAS_MOCK: readonly Conquista[] = [
  {
    id: 'em-chamas',
    titulo: 'Em Chamas',
    descricao: 'Mantenha uma sequência de 7 dias de estudos consecutivos.',
    icone: 'pi pi-bolt',
    tema: 'ambar',
    status: 'desbloqueada',
  },
  {
    id: 'mestre-do-binario',
    titulo: 'Mestre do Binário',
    descricao: 'Obtenha 90% de aproveitamento no módulo Sistemas de Numeração.',
    icone: 'pi pi-microchip-ai',
    tema: 'neutro',
    status: 'em-progresso',
    progresso: 80,
  },
  {
    id: 'arquiteto',
    titulo: 'Arquiteto',
    descricao: 'Acerte 50 questões sobre componentes da CPU da máquina.',
    icone: 'pi pi-microchip',
    tema: 'azul',
    status: 'desbloqueada',
  },
];

@Injectable({ providedIn: 'root' })
export class ConquistasService {
  obterConquistas(): Observable<readonly Conquista[]> {
    return of(CONQUISTAS_MOCK);
  }
}
