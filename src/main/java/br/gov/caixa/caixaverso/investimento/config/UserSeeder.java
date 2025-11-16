package br.gov.caixa.caixaverso.investimento.config;

import br.gov.caixa.caixaverso.investimento.user.data.UserRepository;
import br.gov.caixa.caixaverso.investimento.user.domain.Role;
import br.gov.caixa.caixaverso.investimento.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um usuário de teste "admin" se o banco estiver vazio.
 * A senha é lida das propriedades da aplicação.
 */
@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    // 1. Adiciona um logger SLF4J (Correção do System.out)
    private static final Logger logger = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 2. Injeta a senha a partir do application.properties
    @Value("${application.seeder.admin-password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User(
                    "admin",
                    // 3. Usa a senha injetada
                    passwordEncoder.encode(adminPassword),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);

            // 4. Usa o logger
            logger.info(">>> Usuário 'admin' padrão criado com sucesso.");
        }
    }
}