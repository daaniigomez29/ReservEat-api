package com.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.restaurant.domain.model.GlobalRole;
import com.restaurant.infrastructure.persistence.entity.AuthUserEntity;
import com.restaurant.infrastructure.persistence.repository.JpaAuthUserRepository;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class AuthUserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private JpaAuthUserRepository jpaAuthUserRepository;

    @Test
    void savesAndFindsUserByEmail() {
        AuthUserEntity user = AuthUserEntity.builder()
        .email("demo@test.com")
        .username("demo1")
        .password("hashed")
        .globalRole(GlobalRole.USER)
        .emailVerified(false)
        .build();

        jpaAuthUserRepository.save(user);

        Optional<AuthUserEntity> founded = jpaAuthUserRepository.findByEmail("demo@test.com");

        assertThat(founded).isPresent();
        assertThat(founded.get().getUsername()).isEqualTo("demo1");
        assertThat(founded.get().getId()).isNotNull();
    }
}
