package br.com.cachly.backend.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:cachly@gmail.com}")
    private String remetente;

    @Async
    public void enviarEmailResetSenha(String destinatario, String nome, String linkReset) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("Cachly \u2014 Redefinir sua senha");
            helper.setText(construirHtmlEmail(nome, linkReset), true);

            // Logo inline via CID — funciona em Gmail, Outlook, Apple Mail, etc.
            ClassPathResource logo = new ClassPathResource("templates/cachly-logo.png");
            helper.addInline("cachly-logo", logo, "image/png");

            mailSender.send(mensagem);
            log.info("E-mail de reset de senha enviado para {}", destinatario);
        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail de reset para {}: {}", destinatario, e.getMessage());
        }
    }

    private String construirHtmlEmail(String nome, String linkReset) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,sans-serif;background:#f4f4f7;">
              <div style="max-width:480px;margin:32px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 24px;text-align:center;">
                  <img src="cid:cachly-logo" alt="Cachly" style="height:40px;margin-bottom:8px;" />
                  <p style="color:rgba(255,255,255,0.85);margin:0;font-size:14px;">Aprenda. Pratique. Evolua.</p>
                </div>
                <div style="padding:32px 24px;">
                  <p style="font-size:16px;color:#1e1e2e;">Ol\u00e1, <strong>%s</strong>!</p>
                  <p style="font-size:14px;color:#555;line-height:1.6;">Recebemos uma solicita\u00e7\u00e3o para redefinir a senha da sua conta. Clique no bot\u00e3o abaixo para criar uma nova senha:</p>
                  <div style="text-align:center;margin:28px 0;">
                    <a href="%s" style="display:inline-block;background:#6366f1;color:#fff;text-decoration:none;padding:14px 32px;border-radius:8px;font-size:15px;font-weight:600;">Redefinir minha senha</a>
                  </div>
                  <p style="font-size:13px;color:#888;line-height:1.5;">Este link \u00e9 v\u00e1lido por <strong>30 minutos</strong>. Se voc\u00ea n\u00e3o solicitou a redefini\u00e7\u00e3o, ignore este e-mail \u2014 sua senha permanecer\u00e1 inalterada.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                  <p style="font-size:12px;color:#aaa;text-align:center;">Cachly \u2014 Aprenda. Pratique. Evolua.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nome, linkReset);
    }
}
