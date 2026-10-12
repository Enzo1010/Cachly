package br.com.cachly.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${app.apps-script.secret}")
    private String sharedSecret;

    public EmailService(
            @Value("${app.apps-script.url}") String appsScriptUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.restClient = RestClient.builder()
                .baseUrl(appsScriptUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public void enviarEmailResetSenha(
            String destinatario,
            String nome,
            String linkReset
    ) {
        Map<String, String> payload = Map.of(
                "email", destinatario,
                "resetLink", linkReset,
                "nome", nome,
                "secret", sharedSecret
        );

        try {
            String resposta = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (resposta == null || !resposta.replaceAll("\\s+", "").contains("\"status\":200")) {
                throw new IllegalStateException("O Apps Script não confirmou o envio. Resposta recebida: " + resposta);
            }
        } catch (Exception e) {
            System.err.println("Erro de conexão com Google Apps Script: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na comunicação com o servidor de e-mails.");
        }
    }
}