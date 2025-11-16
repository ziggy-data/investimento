package br.gov.caixa.caixaverso.investimento.user.domain;


import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DisplayName("Teste de Integração - Entidade User (JPA)")
class UserIT {

    @Autowired
    private TestEntityManager entityManager;

    // --- Teste de Sucesso ---

    @Test
    @DisplayName("Deve salvar e recuperar um usuário válido")
    void save_ComUsuarioValido_DevePersistirCorretamente() {
        // Cenário (Arrange)
        User user = new User("admin-valido", "hashed_password", Role.ROLE_ADMIN);

        // Ação (Act)
        User salvo = entityManager.persistAndFlush(user);

        // Verificação (Assert)
        User encontrado = entityManager.find(User.class, salvo.getId());
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getUsername()).isEqualTo("admin-valido");
        assertThat(encontrado.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    // --- Testes de Falha (Regras de Negócio e Dados Incorretos) ---

    @Test
    @DisplayName("Falha (Regra): Deve lançar Exceção para username duplicado")
    void save_ComUsernameDuplicado_DeveLancarExcecao() {
        // Cenário (Arrange)
        entityManager.persistAndFlush(new User("usuarioUnico", "pass1", Role.ROLE_USER));
        User userDuplicado = new User("usuarioUnico", "pass2", Role.ROLE_USER);

        // Ação (Act) & Verificação (Assert)
        // A constraint 'unique' do banco de dados deve ser acionada
        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(userDuplicado);
        });
    }

    @Test
    @DisplayName("Falha (Dados): Deve lançar Exceção para username nulo")
    void save_ComUsernameNulo_DeveLancarExcecao() {
        // Cenário (Arrange)
        User user = new User(null, "pass1", Role.ROLE_USER);

        // Ação (Act) & Verificação (Assert)
        // A constraint '@Column(nullable=false)' deve ser acionada
        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(user);
        });
    }

    @Test
    @DisplayName("Falha (Dados): Deve lançar Exceção para password nulo")
    void save_ComPasswordNulo_DeveLancarExcecao() {
        // Cenário (Arrange)
        User user = new User("userSemSenha", null, Role.ROLE_USER);

        // Ação (Act) & Verificação (Assert)
        // A constraint '@NotNull' e '@Column(nullable=false)' deve ser acionada
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(user);
        });
    }

    @Test
    @DisplayName("Falha (Dados): Deve lançar Exceção para Role nulo")
    void save_ComRoleNulo_DeveLancarExcecao() {
        // Cenário (Arrange)
        User user = new User("userSemRole", "pass1", null);

        // Ação (Act) & Verificação (Assert)
        // A constraint '@NotNull' e '@Column(nullable=false)' deve ser acionada
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(user);
        });
    }
}