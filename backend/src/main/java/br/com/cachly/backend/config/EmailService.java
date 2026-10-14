package br.com.cachly.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${app.apps-script.secret:}")
    private String sharedSecret;

    public EmailService(
            @Value("${app.apps-script.url:https://script.google.com/macros/s/dummy/exec}") String appsScriptUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.restClient = RestClient.builder()
                .baseUrl(appsScriptUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    @Async
    public void enviarEmailResetSenha(
            String destinatario,
            String nome,
            String linkReset
    ) {
        Map<String, String> payload = Map.of(
                "email", destinatario,
                "resetLink", linkReset,
                "nome", nome,
                "secret", sharedSecret != null ? sharedSecret : ""
        );

        try {
            String resposta = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (resposta == null || !resposta.replaceAll("\\s+", "").contains("\"status\":200")) {
                log.warn("O Apps Script não confirmou o envio. Resposta recebida: {}", resposta);
            } else {
                log.info("E-mail de reset de senha enviado com sucesso para {}", destinatario);
            }
        } catch (Exception e) {
            log.error("Erro de conexão com Google Apps Script ao enviar e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}