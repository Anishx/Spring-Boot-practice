package com.drivewealth.testing.containers.config;

import com.drivewealth.testing.containers.ITestContainerAware;
import org.testcontainers.containers.GenericContainer;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractContainerConfig< T extends GenericContainer> implements IContainerConfig<T>, ITestContainerAware {
  private final String imageTag;
  private Map<String, Supplier<String>> propertyBindings;

  protected AbstractContainerConfig(String imageTag, String defaultImageTag) {
    this.imageTag = Objects.requireNonNullElse(imageTag, defaultImageTag);
    this.propertyBindings = new HashMap<>();
  }

  public String getImageTag() {
    return imageTag;
  }


  protected Runnable chain(Runnable first, Runnable second){
    if (first == null && second == null){
      return null;
    }
    if (first == null){
      return second;
    }
    return () -> {
      first.run();
      second.run();
    };
  }

  @Override
  public Map<String, Supplier<String>> getBoundProperties(T container) {
    bindProperties(container);
    return propertyBindings;
  }

  abstract protected void bindProperties(T container);

  protected AbstractContainerConfig bindProperty(String property, Supplier<String> supplier){
    this.propertyBindings.put(property, supplier);
    return this;
  }
}
