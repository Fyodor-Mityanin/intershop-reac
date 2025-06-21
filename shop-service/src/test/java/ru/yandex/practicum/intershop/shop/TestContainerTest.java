package ru.yandex.practicum.intershop.shop;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class TestContainerTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql")
            .withReuse(true);

    @Container
    public static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    private static void postgresqlProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL (R2DBC)
        registry.add("spring.r2dbc.url", () ->
                postgreSQLContainer.getJdbcUrl().replace("jdbc:", "r2dbc:")
        );
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    protected void executeSqlScriptsBlocking(List<String> sqlScripts) {
        Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        Flux.fromIterable(sqlScripts)
                                .map(ClassPathResource::new)
                                .concatMap(resource -> ScriptUtils.executeSqlScript(connection, resource))
                                .thenMany(Mono.from(connection.close()))
                )
                .blockLast();
    }

    protected void cleanupDatabase() {
        executeSqlScriptsBlocking(List.of("/sql/clean_up.sql"));
    }
}