package com.drivewealth.testing.containers.config;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;

public class KafkaContainerConfig extends AbstractContainerConfig<KafkaContainer> {
  private static final String DEFAULT_IMAGE = "confluentinc/cp-kafka:6.0.9";

  public KafkaContainerConfig() {
    this(null);
  }


  public KafkaContainerConfig(String imageTag) {
    super(imageTag, DEFAULT_IMAGE);
  }

  @Override
  public KafkaContainer create(Network network) {
    return new KafkaContainer(DockerImageName.parse( getImageTag()))
        .withNetwork(network)
        .withNetworkAliases("broker");


  }

  @Override
  public Runnable onStartup(KafkaContainer container) {
    return null;
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(KafkaContainer.KAFKA_PORT));
  }

  @Override
  protected void bindProperties(KafkaContainer kafka) {
    bindProperty("spring.kafka.bootstrap-servers", () -> kafka.getBootstrapServers());
  }

}
