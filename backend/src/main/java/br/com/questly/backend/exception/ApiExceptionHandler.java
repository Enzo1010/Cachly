package br.com.questly.backend.exception;

import br.com.questly.backend.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ConflitoDeDadosException.class)
    public ResponseEntity<ErroResponse> tratarConflitoDeDados(
            ConflitoDeDadosException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarViolacaoDeIntegridade(
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                "A operação viola uma regra de integridade dos dados",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                campos.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Existem campos inválidos na requisição",
                request.getRequestURI(),
                campos
        );
    }

    private ResponseEntity<ErroResponse> criarResposta(
            HttpStatus status,
            String mensagem,
            String caminho,
            Map<String, String> campos
    ) {
        ErroResponse erro = new ErroResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                caminho,
                campos
        );

        return ResponseEntity.status(status).body(erro);
    }
}
