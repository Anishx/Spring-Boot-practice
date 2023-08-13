package com.drivewealth.testing.containers.config;

import com.drivewealth.testing.containers.FlywayRunner;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

public abstract class AbstractDatabaseContainerConfig< T extends JdbcDatabaseContainer> extends AbstractContainerConfig<T> {
  private String databaseName;

  private String flywaySchema;

  /**
   *
   * @param imageTag
   * @param defaultImageTag
   * @param flywaySchema Specify where flyway schema tables will live, null will not run flyway
   */
  public AbstractDatabaseContainerConfig(String imageTag, String defaultImageTag, String flywaySchema) {
    super(imageTag, defaultImageTag);
    this.flywaySchema = flywaySchema;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public AbstractDatabaseContainerConfig withDatabaseName(String databaseName){
    this.databaseName = databaseName;
    return this;
  }
  @Override
  public Runnable onStartup(T container) {
    return chain(onDBStartup(container), executeFlyway(container));
  }

  private Runnable executeFlyway(T container) {
    if(flywaySchema == null){
      return null;
    }
    try {
      Class.forName("org.flywaydb.core.Flyway");
      return () -> new FlywayRunner(flywaySchema).run(container);
    } catch (ClassNotFoundException e) {
     throw new RuntimeException("Flyway requested but not found in classpath.");
    }
  }

  protected Runnable onDBStartup(T container){
    return null;
  }

  @Override
  protected void bindProperties(T jdbc) {
    bindProperty("spring.r2dbc.url", () -> "r2dbc:" + StringUtils.removeStart(jdbc.getJdbcUrl(), "jdbc:"));
    bindProperty("spring.datasource.url", jdbc::getJdbcUrl);
    bindProperty("spring.r2dbc.username", jdbc::getUsername);
    bindProperty("spring.r2dbc.password", jdbc::getPassword);
  }
}
