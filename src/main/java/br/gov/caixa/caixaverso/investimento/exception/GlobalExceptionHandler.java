package br.gov.caixa.caixaverso.investimento.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de validação do @Valid (ex: @NotNull, @Min)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ApiErrorResponse errorResponse = new ApiErrorResponse("Erro de Validação", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura requisições para URLs/caminhos que não existem (404).
     * Isso só funciona após adicionar as linhas no application.properties.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Caminho Não Encontrado",
                List.of("O recurso solicitado na URL '" + ex.getRequestURL() + "' não existe.")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); // Retorna 404
    }

    /**
     * Captura nossos erros de regra de negócio (ex: Produto não encontrado)
     */
    @ExceptionHandler(ValidacaoNegocioException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(ValidacaoNegocioException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Erro na Regra de Negócio", List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    /**
     * Captura qualquer outro erro inesperado (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericExceptions(Exception ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Erro Interno do Servidor", List.of("Ocorreu um erro inesperado."));
        // Logar a exceção original (ex.printStackTrace() ou usar um logger)
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Captura erros de tipo em parâmetros de URL (ex: /perfil-risco/abc).
     * Converte a exceção em um 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        Object value = ex.getValue();

        String message = String.format("O parâmetro de URL '%s' está no formato errado. Era esperado um valor do tipo '%s', mas foi recebido: '%s'.",
                paramName, requiredType, value);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Tipo de Parâmetro Inválido",
                List.of(message)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // Retorna 400
    }

    /**
     * Captura falhas de autenticação (ex: senha errada, usuário não existe).
     * Retorna um 401 Unauthorized limpo.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Autenticação Falhou",
                List.of("Usuário ou senha inválidos.")
        );
        // 401 é mais semanticamente correto para falha de login do que 403
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // DTO auxiliar para padronizar as respostas de erro
    public record ApiErrorResponse(String message, List<String> details) {}
}

