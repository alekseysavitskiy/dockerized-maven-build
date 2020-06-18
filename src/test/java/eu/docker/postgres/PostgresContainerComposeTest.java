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
import org.testcontainers.containers.DockerComposeContainer;
import java.io.File;
import org.testcontainers.containers.wait.strategy.Wait;
/**
 * Simple showcase of interacting with a postgres Docker container through the help of
 * testcontainers library.
 */
@Testable
public class PostgresContainerComposeTest {

  static DockerComposeContainer environment =   new DockerComposeContainer(new File("src/test/resources/compose-test.yml"))
          .withExposedService("db", 5432, Wait.forListeningPort());
  @BeforeAll
  static void startUp() {
    environment.start();
  }

  @AfterAll
  static void tearDown() {
    environment.stop();
  }

  @Test
  @DisplayName("when select 1 is executed it is retrieved from postgres")
  public void demo() throws Exception {
    ResultSet resultSet = performQuery(environment, "SELECT 1");
    resultSet.next();
    int result = resultSet.getInt(1);
    assertEquals(1, result);
  }

  private ResultSet performQuery(DockerComposeContainer postgres, String query) throws SQLException {
    String jdbcUrl = "jdbc:postgresql://" + postgres.getServiceHost("db", 5432) + ":" + postgres.getServicePort("db", 5432) + "/ops";
    String username = "ops";
    String password = "opsops";
    Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
    return conn.createStatement()
        .executeQuery(query);
  }
}