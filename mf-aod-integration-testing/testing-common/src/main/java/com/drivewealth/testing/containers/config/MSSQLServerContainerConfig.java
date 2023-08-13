package com.drivewealth.testing.containers.config;

import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.List;

public class MSSQLServerContainerConfig extends AbstractDatabaseContainerConfig<MSSQLServerContainer> {
  public static String DEFAULT_IMAGE = "mcr.microsoft.com/mssql/server";

  public MSSQLServerContainerConfig(String flywaySchema) {
    this(null, flywaySchema);
  }

  public MSSQLServerContainerConfig(String imageTag, String flywaySchema) {
    super(imageTag, DEFAULT_IMAGE, flywaySchema);
  }

  @Override
  public MSSQLServerContainer create(Network network) {

    return new MSSQLServerContainer(getImageTag())
        .acceptLicense()
        .withPassword("MssqlPassword1");
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(1433));
  }

  @Override
  protected void bindProperties(MSSQLServerContainer mssql) {
    bindProperty("spring.datasource.mssql.hikari.url",
        () -> String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false", mssql.getHost(), mssql.getFirstMappedPort(), "master")
    );
    bindProperty("spring.datasource.mssql.hikari.username", mssql::getUsername);
    bindProperty("spring.datasource.mssql.hikari.password", mssql::getPassword);
  }
}
