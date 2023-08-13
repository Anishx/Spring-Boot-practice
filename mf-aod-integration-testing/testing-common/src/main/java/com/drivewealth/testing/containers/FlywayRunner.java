package com.drivewealth.testing.containers;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.JdbcDatabaseContainer;

public class FlywayRunner {
  private final String flywaySchema;

  public FlywayRunner(String flywaySchema) {
    this.flywaySchema = flywaySchema;
  }

  public void run(JdbcDatabaseContainer container){
    var flyway = new Flyway(
        Flyway.configure()
            .dataSource("jdbc:postgresql://172.22.163.221:5432/mf", container.getUsername(), container.getPassword())
//            .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
            .defaultSchema(flywaySchema)
            .locations("classpath:db/migration")
    );
    flyway.migrate();
  }
}
