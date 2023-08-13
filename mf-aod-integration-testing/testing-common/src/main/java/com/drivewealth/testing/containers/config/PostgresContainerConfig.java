package com.drivewealth.testing.containers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;

public class PostgresContainerConfig extends AbstractDatabaseContainerConfig<PostgreSQLContainer> {
  public static final String IMAGE_AURORA = "postgres:14.3-alpine";
  private Logger logger = LoggerFactory.getLogger(getClass());

  public PostgresContainerConfig(String flywaySchema) {
    this(null, flywaySchema);
    withDatabaseName("postgres");
  }

  public PostgresContainerConfig(String imageTag, String flywaySchema) {
    super(imageTag, IMAGE_AURORA, flywaySchema);
  }

  @Override
  public PostgreSQLContainer create(Network network) {
    var container = new PostgreSQLContainer(getImageTag())
        .withDatabaseName(getDatabaseName())
        .withUsername("sa")
        .withPassword("sa");

    container
        .withNetwork(network)
        .withNetworkAliases("db")
        .withCommand("postgres -c log-statement=all");

    return container;
  }



  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(5432));
  }


}
