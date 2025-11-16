package br.gov.caixa.caixaverso.investimento.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Esta classe trata o erro quando um usuário não autenticado
 * tenta acessar um recurso protegido.
 *
 * Por padrão, o Spring redirecionaria para uma página de login (o que causa o 403).
 * Nós o instruímos a simplesmente retornar 401 UNAUTHORIZED.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Envia a resposta 401 e uma mensagem clara
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acesso não autorizado. Você deve fornecer um token JWT válido.");
    }
}