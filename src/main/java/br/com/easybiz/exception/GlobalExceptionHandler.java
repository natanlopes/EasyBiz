package br.com.easybiz.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Erros de Regra de Negócio (ex: "Saldo insuficiente", "Pedido já aceito")
    @ExceptionHandler({RuntimeException.class, IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleRegraDeNegocio(Exception ex) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Regra de Negócio",
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 2. Erros de Validação do DTO (@Valid, @NotNull, @Email)
    // Retorna lista de campos inválidos para o frontend saber o que corrigir
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<String> erros = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .distinct()
                .collect(Collectors.toList());

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Dados Inválidos",
                String.join(", ", erros) // Ex: "email: formato inválido, senha: obrigatória"
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 3. Erro de Autenticação (Token ausente, inválido ou expirado)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationError(AuthenticationException ex) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Não Autenticado",
                "Falha na autenticação. Verifique seu token."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 4. Erro de Autorização (Usuário logado tentando acessar recurso proibido)
    // Ex: Cliente tentando aceitar pedido, ou Prestador tentando mudar logo de outro negócio
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não tem permissão para realizar esta ação."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 5. Erro Inesperado (Bug, Banco fora do ar, NullPointer)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        // Loga o erro no console do servidor para o desenvolvedor ver (não mostra stacktrace pro usuário)
        log.error("ERRO CRÍTICO NO SISTEMA:", ex);

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno",
                "Ocorreu um erro inesperado. Por favor, contate o suporte."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Método auxiliar para formatar a mensagem de erro de validação
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}