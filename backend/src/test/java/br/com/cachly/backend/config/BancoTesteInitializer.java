package br.com.cachly.backend.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/** Impede que testes Spring inicializem um banco de desenvolvimento ou produção. */
public class BancoTesteInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        validarUrl(context.getEnvironment().getProperty("spring.datasource.url", ""));
    }

    static void validarUrl(String url) {
        String banco = url.split("[?;]", 2)[0];
        banco = banco.substring(banco.lastIndexOf('/') + 1);
        if (!url.startsWith("jdbc:postgresql://") || !banco.endsWith("_test")) {
            throw new IllegalStateException("Testes exigem um banco PostgreSQL exclusivo com nome terminado em _test. Configure TEST_DB_URL.");
        }
    }
}
