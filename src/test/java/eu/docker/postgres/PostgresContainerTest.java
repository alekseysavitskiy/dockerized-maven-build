package eu.docker.postgres;


import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Simple showcase of interacting with a postgres Docker container through the help of
 * testcontainers library.
 */
@Testable
public class PostgresContainerTest {

  static PostgreSQLContainer postgresContainer = new PostgreSQLContainer();

  @BeforeAll
  static void startUp() {
    postgresContainer.start();
  }

  @AfterAll
  static void tearDown() {
    postgresContainer.stop();
  }

  @Test
  @DisplayName("when select 1 is executed it is retrieved from postgres")
  public void demo() throws Exception {
    ResultSet resultSet = performQuery(postgresContainer, "SELECT 1");
    resultSet.next();
    int result = resultSet.getInt(1);
    assertEquals(1, result);
  }

  private ResultSet performQuery(PostgreSQLContainer postgres, String query) throws SQLException {
    String jdbcUrl = postgres.getJdbcUrl();
    String username = postgres.getUsername();
    String password = postgres.getPassword();
    Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
    return conn.createStatement()
        .executeQuery(query);
  }
}