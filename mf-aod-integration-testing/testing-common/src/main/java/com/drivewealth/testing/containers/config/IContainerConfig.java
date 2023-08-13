package com.drivewealth.testing.containers.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IContainerConfig<C extends GenericContainer> {


  C create(Network network);

  Runnable onStartup(C container);

  /**
   * Port for static binding; eg testing locally
   * @return
   */
  List<ContainerStaticPortMapping> getStaticPortMappings();

  Map<String, Supplier<String>> getBoundProperties(C container);
}
