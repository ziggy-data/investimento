package br.gov.caixa.caixaverso.investimento.config;

import br.gov.caixa.caixaverso.investimento.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // OTIMIZAÇÃO: Removemos o UserDetailsService (a causa da lentidão)
    // private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Passa para o próximo filtro
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Se o token estiver expirado ou malformado, o extractUsername falha.
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Inválido ou Expirado");
            return;
        }

        // Se o usuário não estiver autenticado na sessão...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // OTIMIZAÇÃO (Stateless):
            // 1. NÃO vamos mais ao banco de dados (sem UserDetailsService).
            // 2. Apenas checamos se o token (que já foi validado pelo extractUsername)
            //    não está expirado.
            if (jwtService.isTokenValid(jwt)) {
                // 3. Extraímos as 'roles' DE DENTRO do token
                List<String> roles = jwtService.extractRoles(jwt);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 4. Autenticamos o usuário usando o username e as roles do token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, // O 'principal' agora é só o username (String)
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}