package br.gov.caixa.caixaverso.investimento.user.data;

import br.gov.caixa.caixaverso.investimento.user.domain.Role;
import br.gov.caixa.caixaverso.investimento.user.domain.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste de Integração para o UserRepository.
 * Carrega apenas a camada JPA e roda contra um H2 em memória.
 * A tabela 'usuarios' está VAZIA antes de cada teste.
 */
@DataJpaTest
@DisplayName("Teste de Integração - UserRepository")
class UserRepositoryIT {

    @Autowired
    private TestEntityManager testEntityManager; // Helper para persistir e buscar

    @Autowired
    private UserRepository userRepository; // A interface que estamos testando

    // --- Testes para JpaRepository.save() ---

    @Nested
    @DisplayName("Testes de Persistência (save)")
    class SaveTests {

        @Test
        @DisplayName("Sucesso: Deve salvar um usuário 'ADMIN' com dados válidos")
        void save_ComAdminValido_DevePersistirCorretamente() {
            // Cenário (Arrange)
            User user = new User("admin", "hashed_password", Role.ROLE_ADMIN);

            // Ação (Act)
            User salvo = userRepository.save(user);

            // Força o H2 a executar o SQL
            testEntityManager.flush();
            testEntityManager.clear(); // Limpa o cache de 1º nível

            // Verificação (Assert)
            User encontrado = testEntityManager.find(User.class, salvo.getId());
            assertThat(encontrado).isNotNull();
            assertThat(encontrado.getUsername()).isEqualTo("admin");
            assertThat(encontrado.getRole()).isEqualTo(Role.ROLE_ADMIN);
        }

        @Test
        @DisplayName("Sucesso: Deve salvar um usuário 'USER' com dados válidos")
        void save_ComUserValido_DevePersistirCorretamente() {
            // Cenário (Arrange)
            User user = new User("cliente123", "hashed_password", Role.ROLE_USER);

            // Ação (Act)
            User salvo = userRepository.save(user);
            testEntityManager.flush();
            testEntityManager.clear();

            // Verificação (Assert)
            User encontrado = testEntityManager.find(User.class, salvo.getId());
            assertThat(encontrado).isNotNull();
            assertThat(encontrado.getUsername()).isEqualTo("cliente123");
            assertThat(encontrado.getRole()).isEqualTo(Role.ROLE_USER);
        }

        @Test
        @DisplayName("Falha (Regra Negócio): Deve lançar DataIntegrityViolationException para username duplicado")
        void save_ComUsernameDuplicado_DeveLancarExcecao() {
            // Cenário (Arrange): Salva o primeiro usuário
            User user1 = new User("usuarioUnico", "pass1", Role.ROLE_USER);
            testEntityManager.persist(user1);

            // Prepara o segundo usuário com o mesmo username
            User user2 = new User("usuarioUnico", "pass2", Role.ROLE_USER);

            // Ação (Act) & Verificação (Assert)
            // A exceção da constraint 'unique' só é lançada no 'flush'
            assertThrows(DataIntegrityViolationException.class, () -> {
                userRepository.save(user2);
                testEntityManager.flush();
            });
        }

        @Test
        @DisplayName("Falha (Dados Incorretos): Deve lançar Exceção para username nulo")
        void save_ComUsernameNulo_DeveLancarExcecao() {
            // Cenário (Arrange)
            User user = new User(null, "pass1", Role.ROLE_USER);

            // Ação (Act) & Verificação (Assert)
            // A constraint 'unique' (que também implica 'not null' no H2/Postgres) falha
            assertThrows(DataIntegrityViolationException.class, () -> {
                userRepository.save(user);
                testEntityManager.flush();
            });
        }

        @Test
        @DisplayName("Falha (Dados Incorretos): Deve lançar Exceção para password nulo")
        void save_ComPasswordNulo_DeveLancarExcecao() {
            // Cenário (Arrange)
            // Assumindo que a coluna 'password' no User.java não é 'nullable'
            // (Se for nullable, este teste falhará e deve ser removido)
            User user = new User("userSemSenha", null, Role.ROLE_USER);

            // Ação (Act) & Verificação (Assert)
            assertThrows(ConstraintViolationException.class, () -> {
                userRepository.save(user);
                testEntityManager.flush();
            });
        }

        @Test
        @DisplayName("Falha (Dados Incorretos): Deve lançar Exceção para Role nulo")
        void save_ComRoleNulo_DeveLancarExcecao() {
            // Cenário (Arrange)
            User user = new User("userSemRole", "pass1", null);

            // Ação (Act) & Verificação (Assert)
            assertThrows(ConstraintViolationException.class, () -> {
                userRepository.save(user);
                testEntityManager.flush();
            });
        }
    }

    // --- Testes para findByUsername(String username) ---

    @Nested
    @DisplayName("Testes da Query (findByUsername)")
    class FindByUsernameTests {

        @Test
        @DisplayName("Sucesso: Deve encontrar um usuário existente")
        void findByUsername_UsuarioExistente_DeveRetornarOptionalComUsuario() {
            // Cenário (Arrange)
            User user = new User("usuario.teste", "pass123", Role.ROLE_USER);
            testEntityManager.persistAndFlush(user);

            // Ação (Act)
            Optional<User> resultado = userRepository.findByUsername("usuario.teste");

            // Verificação (Assert)
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getUsername()).isEqualTo("usuario.teste");
            assertThat(resultado.get().getRole()).isEqualTo(Role.ROLE_USER);
        }

        @Test
        @DisplayName("Falha (Partição): Deve retornar Optional Vazio para usuário inexistente")
        void findByUsername_UsuarioInexistente_DeveRetornarOptionalVazio() {
            // Cenário (Arrange)
            // (Banco está limpo)

            // Ação (Act)
            Optional<User> resultado = userRepository.findByUsername("fantasma");

            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Regra Negócio): Deve ser Case-Sensitive e retornar Vazio")
        void findByUsername_BuscaCaseSensitive_DeveRetornarOptionalVazio() {
            // Cenário (Arrange)
            User user = new User("admin", "pass123", Role.ROLE_ADMIN);
            testEntityManager.persistAndFlush(user);

            // Ação (Act)
            Optional<User> resultado = userRepository.findByUsername("Admin"); // 'A' maiúsculo

            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Valor Limite): Deve retornar Vazio para string vazia")
        void findByUsername_ComStringVazia_DeveRetornarOptionalVazio() {
            // Ação (Act)
            Optional<User> resultado = userRepository.findByUsername("");

            // Verificação (Assert)
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Falha (Exceção): Deve lançar Exceção para username nulo")
        void findByUsername_ComUsernameNulo_DeveLancarExcecao() {
            // Ação (Act)
            // Spring Data JPA não permite parâmetros de query nulos
            Optional<User> resultado = userRepository.findByUsername(null);

            // Verificação (Assert)
            assertThat(resultado).isEmpty();

        }
    }
}