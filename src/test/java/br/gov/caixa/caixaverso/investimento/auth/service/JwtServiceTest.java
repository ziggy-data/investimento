package br.gov.caixa.caixaverso.investimento.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - JwtService (Stateless)")
class JwtServiceTest {

    // A classe que estamos testando
    private JwtService jwtService;

    @Mock
    private UserDetails mockUserDetails;

    private final String testSecretKey = "NzE0MzU0NzYzNTc2NTIzNjQ1Mjc4QTM5NDE0NDIzNERGMkFENzEzOTRFNjJFNUY3NjM1RjYxNkUyNTM4NTM=";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Injeta os valores @Value
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // Prepara os mocks do usuário (com 'lenient' para evitar erros de stubbing)
        lenient().when(mockUserDetails.getUsername()).thenReturn("testuser");

        // CORREÇÃO: O novo generateToken() agora chama getAuthorities()
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        // O cast (Collection) é necessário para o compilador
        lenient().when(mockUserDetails.getAuthorities()).thenReturn((Collection) authorities);
    }

    // --- Testes de Sucesso (Caminho Feliz) ---

    @Test
    @DisplayName("Deve gerar um token que contém o username e as roles")
    void generateToken_DeveGerarTokenComUsernameERoles() {
        // Ação
        String token = jwtService.generateToken(mockUserDetails);

        // Verificação
        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(List.of("ROLE_USER"), jwtService.extractRoles(token));
    }

    @Test
    @DisplayName("Deve extrair o username correto de um token válido")
    void extractUsername_DeveExtrairUsernameCorreto() {
        // Cenário
        String token = jwtService.generateToken(mockUserDetails);

        // Ação
        String usernameExtraido = jwtService.extractUsername(token);

        // Verificação
        assertEquals("testuser", usernameExtraido);
    }

    @Test
    @DisplayName("Deve extrair as roles corretas de um token válido")
    void extractRoles_DeveExtrairRolesCorretamente() {
        // Cenário
        String token = jwtService.generateToken(mockUserDetails);

        // Ação
        List<String> roles = jwtService.extractRoles(token);

        // Verificação
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    @DisplayName("Deve retornar true para um token válido e não expirado")
    void isTokenValid_DeveRetornarTrue_ParaTokenCorreto() {
        // Cenário
        String token = jwtService.generateToken(mockUserDetails);

        // Ação
        boolean isValid = jwtService.isTokenValid(token);

        // Verificação
        assertTrue(isValid);
    }

    // --- Testes de Falha (Regras de Negócio e Dados Incorretos) ---

    @Test
    @DisplayName("Deve lançar ExpiredJwtException ao validar token expirado")
    void isTokenValid_DeveLancarExcecao_ParaTokenExpirado() throws InterruptedException {
        // Cenário (Falha: Dados Incorretos)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // Expira em 1ms
        String tokenExpirado = jwtService.generateToken(mockUserDetails);

        Thread.sleep(50); // Espera o token expirar

        // Ação & Verificação
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(tokenExpirado);
        });
    }

    @Test
    @DisplayName("Deve lançar MalformedJwtException ao tentar extrair de um token malformado")
    void extractUsername_DeveLancarExcecao_ParaTokenMalformado() {
        // Cenário (Falha: Dados Incorretos)
        String tokenMalformado = "nao.e.um.token.jwt";

        // Ação & Verificação
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUsername(tokenMalformado);
        });
    }

    @Test
    @DisplayName("Deve lançar MalformedJwtException ao tentar extrair roles de token malformado")
    void extractRoles_DeveLancarExcecao_ParaTokenMalformado() {
        // Cenário (Falha: Dados Incorretos)
        String tokenMalformado = "nao.e.um.token.jwt";

        // Ação & Verificação
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractRoles(tokenMalformado);
        });
    }
}