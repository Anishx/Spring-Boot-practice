package com.drivewealth.testing.containers.config;

import com.drivewealth.testing.containers.SFTPContainer;
import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SFTPContainerConfig extends AbstractContainerConfig<SFTPContainer>{
  private Set<String> directories;

  protected SFTPContainerConfig(String imageTag) {
    super(imageTag, "atmoz/sftp");
    directories = new HashSet<>();
  }
  public SFTPContainerConfig( ) {
    this(null);
  }


  public SFTPContainerConfig withDirectories(String... dirs){
    directories.addAll(Arrays.asList(dirs));
    return this;
  }

  @Override
  public SFTPContainer create(Network network) {
    return new SFTPContainer(getImageTag());
  }

  @Override
  public Runnable onStartup(SFTPContainer container) {
    return () -> {
      container.mkdir(directories.toArray(directories.toArray(new String[0])));
    };
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(new ContainerStaticPortMapping(2222, 22));
  }

  @Override
  protected void bindProperties(SFTPContainer sftp) {
    bindProperty("sftp.port", () -> sftp.getMappedPort(22) + "");
    bindProperty("sftp.user", () -> sftp.getUser());
    bindProperty("sftp.password", () -> sftp.getPassword());
    bindProperty("sftp.host", () -> sftp.getHost());
  }
}
