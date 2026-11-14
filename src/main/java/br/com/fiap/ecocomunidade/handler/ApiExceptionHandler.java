package br.com.fiap.ecocomunidade.handler;

import br.com.fiap.ecocomunidade.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    public record Erro(Instant timestamp, int status, String mensagem, String caminho, List<String> detalhes) {}
    private ResponseEntity<Erro> erro(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        return ResponseEntity.status(status).body(new Erro(Instant.now(), status.value(), message, request.getRequestURI(), details));
    }
    @ExceptionHandler({NaoEncontradoException.class, NoResourceFoundException.class})
    ResponseEntity<Erro> notFound(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.NOT_FOUND, e instanceof NaoEncontradoException ? e.getMessage() : "Rota nao encontrada.", r, List.of());
    }
    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<Erro> conflict(RegraNegocioException e, HttpServletRequest r) { return erro(HttpStatus.CONFLICT, e.getMessage(), r, List.of()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Erro> integrity(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.CONFLICT, "Registro duplicado, referencia invalida ou registro com dependencias.", r, List.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Erro> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_REQUEST, "Dados invalidos.", r, e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList());
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class, MissingServletRequestParameterException.class})
    ResponseEntity<Erro> badRequest(Exception e, HttpServletRequest r) { return erro(HttpStatus.BAD_REQUEST, "JSON, parametro ou formato de data invalido.", r, List.of()); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Erro> method(HttpRequestMethodNotSupportedException e, HttpServletRequest r) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).allow(e.getSupportedHttpMethods().toArray(HttpMethod[]::new))
            .body(new Erro(Instant.now(), 405, "Metodo HTTP nao permitido.", r.getRequestURI(), List.of()));
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Erro> media(Exception e, HttpServletRequest r) { return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Utilize Content-Type application/json.", r, List.of()); }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Erro> database(Exception e, HttpServletRequest r) { return erro(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponivel.", r, List.of()); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Erro> unexpected(Exception e, HttpServletRequest r) {
        log.error("Falha inesperada no processamento da requisicao", e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisicao.", r, List.of());
    }
}
