package org.seitmolabs;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class SystemCategoryMigrationIntegrationTest {

    @Container
    private static final GenericContainer<?> POSTGRES = new GenericContainer<>(
            DockerImageName.parse("postgres:18.6-alpine3.24"))
            .withEnv("POSTGRES_PASSWORD", "test")
            .withExposedPorts(5432);

    @Test
    void upgradePreservesCategoryIdAndFreshInstallCreatesSystemCategory() throws Exception {
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":"
                + POSTGRES.getMappedPort(5432) + "/postgres";
        Flyway.configure().dataSource(url, "postgres", "test")
                .schemas("upgrade_test").target("3").load().migrate();

        try (var connection = DriverManager.getConnection(url, "postgres", "test");
             var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO upgrade_test.categories (id, name, description) "
                    + "VALUES (42, 'Бронирование', 'Описание')");

            Flyway upgrade = Flyway.configure().dataSource(url, "postgres", "test")
                    .schemas("upgrade_test").load();
            upgrade.migrate();
            try (var result = statement.executeQuery(
                    "SELECT id FROM upgrade_test.categories WHERE system_code = 'BOOKING'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getLong("id")).isEqualTo(42L);
                assertThat(result.next()).isFalse();
            }
            statement.executeUpdate("UPDATE upgrade_test.categories SET name = 'Мои билеты' WHERE id = 42");
            upgrade.migrate();
            try (var result = statement.executeQuery(
                    "SELECT name FROM upgrade_test.categories WHERE system_code = 'BOOKING'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("name")).isEqualTo("Мои билеты");
            }

            Flyway.configure().dataSource(url, "postgres", "test")
                    .schemas("fresh_test").load().migrate();
            try (var result = statement.executeQuery(
                    "SELECT count(*) FROM fresh_test.categories WHERE system_code = 'BOOKING'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getLong(1)).isEqualTo(1L);
            }
        }
    }
}
