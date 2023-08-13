package com.drivewealth.testing.containers.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ActiveMQContainerConfig extends AbstractContainerConfig<GenericContainer> {

  public static final String DEFAULT_IMAGE = "rmohr/activemq:latest";
  private static final Integer DEFAULT_PORT = 61616;
  private static final int DEFAULT_WEB_PORT = 8161;

  public ActiveMQContainerConfig(){
    this(null);
  }

  protected ActiveMQContainerConfig(String imageTag) {
    super(imageTag, DEFAULT_IMAGE);
  }

  @Override
  public GenericContainer create(Network network) {
    return new GenericContainer(getImageTag())
            .withExposedPorts(DEFAULT_PORT, DEFAULT_WEB_PORT)
            .withStartupTimeout(Duration.ofMinutes(5));
  }

  @Override
  public Runnable onStartup(GenericContainer container) {
    return null;
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(DEFAULT_PORT), new ContainerStaticPortMapping(DEFAULT_WEB_PORT));
  }

  @Override
  protected void bindProperties(final GenericContainer container) {
    bindProperty("spring.activemq.broker-url", () -> String.format("tcp://%s:%s", container.getHost(), container.getFirstMappedPort()));
  }
}
