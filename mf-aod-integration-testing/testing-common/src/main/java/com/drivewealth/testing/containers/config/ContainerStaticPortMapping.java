package com.drivewealth.testing.containers.config;

public class ContainerStaticPortMapping {
  private int hostPort;
  private int containerPort;


  public ContainerStaticPortMapping(int hostPort) {
    this(hostPort, hostPort);
  }

  public ContainerStaticPortMapping(int hostPort, int containerPort) {
    this.hostPort = hostPort;
    this.containerPort = containerPort;
  }

  public int getHostPort() {
    return hostPort;
  }

  public int getContainerPort() {
    return containerPort;
  }
}
