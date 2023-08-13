package com.drivewealth.testing.containers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class LocalStackContainerConfig extends AbstractContainerConfig<LocalStackContainer>{
  private Logger logger = LoggerFactory.getLogger(getClass());
  public static final String DEFAULT_IMAGE = "localstack/localstack:1.4.0";
  private Set<LocalStackContainer.Service> services;
  private Set<String> buckets;

  private Set<String> dynamoTables;

  private String region;

  public LocalStackContainerConfig() {
    this(null);
  }

  public LocalStackContainerConfig(String imageTag) {
    super(imageTag, DEFAULT_IMAGE);
    services = new HashSet<>();
    buckets = new HashSet<>();
    dynamoTables = new HashSet<>();
    region = "us-east-1";
  }

  public LocalStackContainerConfig withServices(LocalStackContainer.Service... services){
    this.services.addAll(Arrays.asList(services));
    return this;
  }

  public LocalStackContainerConfig withBuckets(String... buckets){
    this.buckets.addAll(Arrays.asList(buckets));
    this.services.add(S3);
    return this;
  }

  public LocalStackContainerConfig withDynamoTables(String... tables){
    this.dynamoTables.addAll(Arrays.asList(tables));
    this.services.add(DYNAMODB);
    return this;
  }

  public LocalStackContainerConfig withRegion(String region){
    this.region = region;
    return this;
  }
  @Override
  public LocalStackContainer create(Network network) {
    return new LocalStackContainer(DockerImageName.parse(getImageTag()))
      .withServices(services.toArray(new LocalStackContainer.Service[]{}))
      .withEnv("PROVIDER_OVERRIDE_S3", "asf")
      .withEnv("DEBUG", "1");
  }

  @Override
  public Runnable onStartup(LocalStackContainer container) {
    return () ->{
      if (services.contains(S3)){
        initS3(container);
      }
      if(services.contains(DYNAMODB)){
        initDynamoDB(container);
      }


    };
  }

  private void initDynamoDB(LocalStackContainer container) {
    logger.info("DynamoDB Service - accessKey: [{}] secretKey: [{}] region:[{}]", container.getAccessKey(), container.getSecretKey(), container.getRegion());
    dynamoTables.stream().forEach( table ->{
      logger.info("Creating dynamodb table: {}", table);
      String commandFormat = "awslocal dynamodb create-table --table-name %s --key-schema AttributeName=id,KeyType=HASH --attribute-definitions AttributeName=id,AttributeType=S --billing-mode PAY_PER_REQUEST --region %s";
      exec(container, String.format(commandFormat, table, region));
    });

  }

  private void initS3(LocalStackContainer container) {
    logger.info("S3 Service - accessKey: [{}] secretKey: [{}] region:[{}]", container.getAccessKey(), container.getSecretKey(), container.getRegion());
    buckets.stream().forEach( bucket -> {
      logger.info("Creating bucket: {}", bucket);
      exec(container, "awslocal s3 mb s3://" + bucket);
    });
  }

  @Override
  public List<ContainerStaticPortMapping> getStaticPortMappings() {
    return Arrays.asList(
        new ContainerStaticPortMapping(4566)
    );
  }

  @Override
  protected void bindProperties(LocalStackContainer localStack) {
    bindProperty("cloud.aws.s3.endpoint", () -> {
      try {
        return localStack.getEndpointOverride(LocalStackContainer.Service.S3).toURL().toExternalForm();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    });
    bindProperty("cloud.aws.credentials.access-key", () -> localStack.getAccessKey());
    bindProperty("cloud.aws.credentials.secret-key", () -> localStack.getSecretKey());
    bindProperty("cloud.aws.region.static", () -> localStack.getRegion());
  }
}
