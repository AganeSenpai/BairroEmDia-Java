package br.com.fiap.bairro.web;

import br.com.fiap.bairro.service.RegraNegocioException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.TreeMap;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ErrosController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException erro, HttpServletRequest request) {
        var campos = new TreeMap<String, String>();
        erro.getBindingResult().getFieldErrors().forEach(campo -> campos.put(campo.getField(), campo.getDefaultMessage()));
        var resposta = resposta(HttpStatus.BAD_REQUEST, "Verifique os campos enviados.", request);
        resposta.getBody().setProperty("campos", campos);
        return resposta;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ProblemDetail> inexistente(EntityNotFoundException erro, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, erro.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ProblemDetail> negocio(RegraNegocioException erro, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, erro.getMessage(), request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    ResponseEntity<ProblemDetail> conflito(Exception erro, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de dados. Atualize a consulta e verifique os vínculos e valores únicos.", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ProblemDetail> login(BadCredentialsException erro, HttpServletRequest request) {
        return resposta(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> acesso(AccessDeniedException erro, HttpServletRequest request) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso não permitido.", request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> formato(Exception erro, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON, parâmetro ou valor de enum inválido.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> argumento(IllegalArgumentException erro, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, erro.getMessage(), request);
    }

    private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String detalhe, HttpServletRequest request) {
        var problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setInstance(java.net.URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problema);
    }
}
