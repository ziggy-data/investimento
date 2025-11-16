package br.gov.caixa.caixaverso.investimento.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste Unitário - Domínio User")
class UserTest {

    // --- Testes de Sucesso (Regras de Negócio) ---

    @Test
    @DisplayName("Deve construir um usuário 'ADMIN' e atribuir campos corretamente")
    void construtor_DeveAtribuirCamposCorretamente_ParaAdmin() {
        // Cenário (Arrange)
        User user = new User("admin", "hashed_pass", Role.ROLE_ADMIN);

        // Ação & Verificação (Act & Assert)
        assertEquals("admin", user.getUsername());
        assertEquals("hashed_pass", user.getPassword());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
    }

    @Test
    @DisplayName("Deve construir um usuário 'USER' e atribuir campos corretamente")
    void construtor_DeveAtribuirCamposCorretamente_ParaUser() {
        // Cenário (Arrange) - (Partição de Equivalência)
        User user = new User("cliente", "hashed_pass", Role.ROLE_USER);

        // Ação & Verificação (Act & Assert)
        assertEquals("cliente", user.getUsername());
        assertEquals("hashed_pass", user.getPassword());
        assertEquals(Role.ROLE_USER, user.getRole());
    }

    @Test
    @DisplayName("Deve retornar a 'ROLE_ADMIN' correta em getAuthorities")
    void getAuthorities_DeveRetornarRoleAdmin_QuandoForAdmin() {
        // Cenário (Arrange) - (Regra de Negócio)
        User user = new User("admin", "pass", Role.ROLE_ADMIN);

        // Ação (Act)
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Verificação (Assert)
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Deve retornar a 'ROLE_USER' correta em getAuthorities")
    void getAuthorities_DeveRetornarRoleUser_QuandoForUser() {
        // Cenário (Arrange) - (Partição de Equivalência)
        User user = new User("cliente", "pass", Role.ROLE_USER);

        // Ação (Act)
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Verificação (Assert)
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Deve retornar 'true' para todos os status da conta (Regra do PDF)")
    void statusDaConta_DeveSempreRetornarTrue() {
        // Cenário (Arrange)
        User user = new User("user", "pass", Role.ROLE_USER);

        // Ação & Verificação (Act & Assert) - (Cobertura de Ramos)
        assertTrue(user.isAccountNonExpired(), "isAccountNonExpired");
        assertTrue(user.isAccountNonLocked(), "isAccountNonLocked");
        assertTrue(user.isCredentialsNonExpired(), "isCredentialsNonExpired");
        assertTrue(user.isEnabled(), "isEnabled");
    }

    // --- Testes de Falha (Nível Unitário) ---

    @Test
    @DisplayName("Deve permitir a criação com campos nulos (Validação é do BD, não do Java)")
    void construtor_DevePermitirCamposNulos() {
        // Cenário (Arrange) - (Partição de Equivalência: Nulos)
        // O construtor Java *permite* nulos. A *validação* ocorre no BD.
        User user = new User(null, null, null);

        // Ação & Verificação (Act & Assert)
        assertNull(user.getUsername());
        assertNull(user.getPassword());

        // Tentativa de chamar .name() em 'role' (que é nulo)
        assertThrows(NullPointerException.class, () -> {
            user.getAuthorities();
        });
    }
}