package com.drivewealth.testing.containers;

import com.drivewealth.testing.containers.config.AbstractDatabaseContainerConfig;
import com.drivewealth.testing.containers.config.ContainerStaticPortMapping;
import com.drivewealth.testing.containers.config.IContainerConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ContainerPort;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TODO make this a junit extension so it can cleanup after itself
 * TODO reusable...
 */
public class ContainerSetup implements ITestContainerAware {
  private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
  public static final String SYSTEM_PROPERTY_STATIC_PORTS = "staticPorts";

  public static final String SYSTEM_PROPERTY_STATIC_PORTS_ALIAS = "static";
  private List<GenericContainer> containers = new ArrayList<>();

  private List<Runnable> postStartup = new ArrayList<>();

  private Map<String, Map<Integer, Integer>> mappingOverrides = new HashMap<>();

  private boolean staticPorts;

  private volatile boolean started = false;

  private List<IContainerConfig> configs;

  private Map<IContainerConfig, GenericContainer> configContainerMap;

  private Network network;

  public ContainerSetup(IContainerConfig... configs) {
    this(isSystemPropertyStaticPorts(), configs);
  }

  public ContainerSetup(boolean staticPorts, IContainerConfig... configs) {
    this.staticPorts = staticPorts;
    this.configs = new ArrayList<>(Arrays.asList(configs));
    this.configContainerMap = new ConcurrentHashMap<>();
  }

  /**
   * Only run the database
   *
   * @return
   */
  public ContainerSetup withDBOnly() {
    this.configs = this.configs.stream()
        .filter( config -> config instanceof AbstractDatabaseContainerConfig)
        .collect(Collectors.toList());
    return this;
  }

  public ContainerSetup withConfigs(IContainerConfig... configs){
    this.configs.addAll(Arrays.asList(configs));
    return this;
  }



  GenericContainer bindContainer(GenericContainer container, ContainerStaticPortMapping mapping) {
    int containerPort = mapping.getContainerPort(), hostPort = mapping.getHostPort();
    if (isStaticPorts() == false) {
      return container;
    }

    container.getPortBindings().add(hostPort + ":" + containerPort);
    if (containerPort != hostPort) {
      Map<Integer, Integer> overrides = mappingOverrides.computeIfAbsent(container.getDockerImageName(), (k) -> new HashMap<>());
      overrides.put(containerPort, hostPort);
    }

    return container;
  }


  protected void onStartup(GenericContainer container, Runnable action) {
    if (action == null){
      return;
    }
    postStartup.add(() -> {
      if (container.isRunning() == false) {
        return;
      }
      action.run();
    });
  }



  /**
   * Allow to map all exposed ports to the host
   * -DstaticPorts
   *
   * @return
   */
  static boolean isSystemPropertyStaticPorts() {
    boolean isStatic = isSystemPropertySet(SYSTEM_PROPERTY_STATIC_PORTS) || isSystemPropertySet(SYSTEM_PROPERTY_STATIC_PORTS_ALIAS);
    if (isStatic == false) {
      org.slf4j.Logger logger = LoggerFactory.getLogger(ContainerSetup.class);
      logger.info("***Enable staticPorts for development by sending in: -D{} or -D{}", SYSTEM_PROPERTY_STATIC_PORTS, SYSTEM_PROPERTY_STATIC_PORTS_ALIAS);
    }

    return isStatic;

  }

  static boolean isSystemPropertySet(String property) {
    return System.getProperty(property) != null;
  }

  public void configure() {
    if (this.started) {
      return;
    }
    if (isStaticPortAndStarted()) return;

    logger.info("Configured: {} containers to start.", configs.size());
  }

  public boolean isStaticPorts() {
    return this.staticPorts;
  }

  private boolean isStaticPortAndStarted() {
    if (isStaticPorts()) {
      logger.warn(">>>>>Static Ports are Requested, you can't run parallel tests this way.<<<<<");
      if (isStarted()) {
        logger.info("Environment already started since static ports are requested.");
        this.started = true;
        return true;
      }
    }
    return false;
  }

  private boolean isStarted() {
    DockerClient client = DockerClientFactory.lazyClient();
    List<com.github.dockerjava.api.model.Container> runningContainers = client.listContainersCmd().exec();
    for (com.github.dockerjava.api.model.Container runningContainer : runningContainers) {
      Optional<GenericContainer> opt = containers.stream().filter(conf -> conf.getImage().get().equals(runningContainer.getImage())).findFirst();
      if (opt.isEmpty()) {
        continue;
      }
      GenericContainer configuredContainer = opt.get();
      Set<Integer> ports = new HashSet<>(configuredContainer.getExposedPorts());
      ContainerPort[] runningPorts = runningContainer.getPorts();
      for (ContainerPort port : runningPorts) {
        if (port.getPublicPort() != null && ports.contains(port.getPublicPort())) {
          logger.info("Image: {} Found Colliding Port at: {}", runningContainer.getImage(), port.getPublicPort());
          return true;
        }
      }
    }

    return false;
  }

  protected <C extends GenericContainer<C>> C findContainerForPortMapping(Class<C> clazz, boolean required) {
    Optional<GenericContainer> opt = containers.stream().filter(
        c -> clazz.isAssignableFrom(c.getClass())
    ).findAny();
    if (opt.isEmpty()) {
      if (required) {
        throw new IllegalArgumentException("Expected to find container of type " + clazz.getName() + " configured.");
      } else {
        return null;
      }
    }
    return containerMappedPort(clazz.cast(opt.get()));
  }

  public void start() {
    if (this.started) {
      return;
    }
    this.started = true;
    if (configs.isEmpty()) {
      logger.info("****No containers setup for starting.");
      return;
    }

    this.network = Network.newNetwork();
    logger.info("Docker User Network ID: {}", this.network.getId());
    configs.parallelStream().forEach(config -> {
      GenericContainer container = config.create(network);
      config.getStaticPortMappings().stream()
              .forEach(m -> bindContainer(container, ContainerStaticPortMapping.class.cast(m)));
      onStartup(container, config.onStartup(container));
      configContainerMap.put(config, container);
      logger.info("Starting: ({}) Exposed Ports: {}", container.getDockerImageName(), container.getExposedPorts());
      container.start();
    });

    configContainerMap.values().forEach(c -> {
      logger.info("Waiting for: {} to start services.", c.getDockerImageName());
      c.waitingFor(Wait.forListeningPort());
      logger.info("**Container {} is ready! Ports: {} Container: {}", c.getDockerImageName(), c.getPortBindings(), c.getContainerId());
    });

    postStartup.parallelStream().forEach(r -> r.run());
    postStartup.clear();
  }

  public void initialize(DynamicPropertyRegistry registry) {
    configure();
    start();
    setDynamicProperties(registry);
  }

  private void setDynamicProperties(DynamicPropertyRegistry registry) {
    configContainerMap.entrySet().forEach( entry ->{
      IContainerConfig config = entry.getKey();
      GenericContainer container = entry.getValue();
      Map<String, Supplier<String>> properties = config.getBoundProperties(container);
      properties.forEach( (key, value) -> {
        bindProperty(registry, key, value);
      });
    });
  }

  protected void bindProperty(DynamicPropertyRegistry registry, String property, Supplier<String> fn) {
    String value = fn.get();
    logger.info("Dynamic Property: Binding {}={}", property, value);
    if (registry == null){
      return;
    }
    registry.add(property, () -> value);
  }


  /**
   * Intercept calls when we have already running containers to prebind to the existing exposed ports.
   * <br/>
   * Right now all ports are unique with no collisions so not going to work hard to configure explicit
   * mappings.
   *
   * @param container
   * @param <C>
   * @return
   */
  private <C extends GenericContainer<C>> C containerMappedPort(C container) {
    C spied = Mockito.spy(container);
    Mockito.doAnswer(answer -> {
      if (container.isRunning() == false) {
        //we are in static port mode and containers were not started on this session
        int containerPort = answer.getArgument(0);
        Map<Integer, Integer> overrides = mappingOverrides.get(container.getDockerImageName());
        if (overrides == null || overrides.containsKey(containerPort) == false) {
          return containerPort;
        }

        int hostPort = overrides.get(containerPort);
        logger.info("STATIC-PORTS-ENABLED: Container:{} ContainerPort:{} remapped to {}", container.getDockerImageName(), containerPort, hostPort);
        return hostPort;
      }
      return answer.getMethod().invoke(container, answer.getArguments());
    }).when(spied).getMappedPort(Mockito.anyInt());


    return spied;
  }

  public void stop() {
    logger.info("Attempting to shutdown testing containers");
    containers.parallelStream().forEach(c -> {
      if (c.isRunning() == false) {
        return;
      }
      c.stop();
    });
  }

  public JdbcDatabaseContainer getDatabaseContainer() {
    return this.findContainerForPortMapping(JdbcDatabaseContainer.class, true);
  }

  /**
   * Minimal dependencies - slf4j is really chatty so set your root logger explicitly
   * @param root
   */
  protected void setRootLogger(Logger root){

  }

  public void launch(String[] args){
    Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    setRootLogger(root);

    initialize(null);
    try {
      new CountDownLatch(1).await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    new ContainerSetup(true).launch(args);
  }
}
