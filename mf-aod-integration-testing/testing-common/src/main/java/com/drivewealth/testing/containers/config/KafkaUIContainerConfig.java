package com.drivewealth.testing.containers.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaUIContainerConfig extends AbstractContainerConfig<GenericContainer> {
  public KafkaUIContainerConfig() {
    super(null, "provectuslabs/kafka-ui:latest");
  }

  @Override
  protected void bindProperties(GenericContainer container) {

  }

  @Override
  public GenericContainer create(Network network) {
    Map<String, String> env = new HashMap<>();
    env.put("KAFKA_CLUSTERS_0_NAME", "local");
    env.put("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", "BROKER://broker:9092");

    return new GenericContainer<>(getImageTag())
        .withNetwork(network)
        .withEnv(env)
        ;
  }

  @Override
  public Runnable onStartup(GenericContainer container) {
    return null;
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(8888, 8080));
  }
}
