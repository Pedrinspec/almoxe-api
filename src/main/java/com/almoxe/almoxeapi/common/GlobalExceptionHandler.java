package com.almoxe.almoxeapi.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(),
                        "Recurso não encontrado", List.of(ex.getMessage())));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.unprocessableEntity().body(
                new ErrorResponse(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Regra de negócio violada", List.of(ex.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(),
                        "Conflito de dados",
                        List.of("A operação viola uma restrição do banco (unicidade ou referência).")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                        "Corpo da requisição inválido",
                        List.of("JSON malformado ou valor incompatível com o tipo esperado.")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação", erros));
    }
}
