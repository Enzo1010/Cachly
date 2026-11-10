package br.com.cachly.backend.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BancoTesteInitializerTest {
    @Test
    void deveRecusarBancoCompartilhado() {
        assertThrows(IllegalStateException.class, () -> BancoTesteInitializer.validarUrl("jdbc:postgresql://localhost:5432/cachly"));
        assertThrows(IllegalStateException.class, () -> BancoTesteInitializer.validarUrl("jdbc:postgresql://localhost/cachly?schema=_test"));
        assertThrows(IllegalStateException.class, () -> BancoTesteInitializer.validarUrl(""));
    }

    @Test
    void deveAceitarSomenteBancoExclusivoDeTeste() {
        assertDoesNotThrow(() -> BancoTesteInitializer.validarUrl("jdbc:postgresql://localhost:5432/cachly_test?sslmode=disable"));
    }
}
