export function configurarArmazenamentosTeste(): void {
  Object.defineProperty(window, 'localStorage', {
    configurable: true,
    value: criarArmazenamento(),
  });
  Object.defineProperty(window, 'sessionStorage', {
    configurable: true,
    value: criarArmazenamento(),
  });
}

function criarArmazenamento(): Storage {
  const itens = new Map<string, string>();

  return {
    get length(): number {
      return itens.size;
    },
    clear: () => itens.clear(),
    getItem: (chave) => itens.get(chave) ?? null,
    key: (indice) => Array.from(itens.keys())[indice] ?? null,
    removeItem: (chave) => itens.delete(chave),
    setItem: (chave, valor) => itens.set(chave, valor),
  };
}
