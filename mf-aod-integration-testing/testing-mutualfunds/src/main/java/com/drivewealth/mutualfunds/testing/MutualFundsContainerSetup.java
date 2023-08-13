package com.drivewealth.mutualfunds.testing;

import ch.qos.logback.classic.Level;
import com.drivewealth.testing.containers.ContainerSetup;
import com.drivewealth.testing.containers.config.*;
import org.slf4j.Logger;
import org.testcontainers.containers.localstack.LocalStackContainer;

public class MutualFundsContainerSetup extends ContainerSetup {
  public MutualFundsContainerSetup(boolean staticPorts) {
    super(
        staticPorts,
        new PostgresContainerConfig(PostgresContainerConfig.IMAGE_AURORA, "mf").withDatabaseName("mf")
        , new KafkaContainerConfig()
        , new LocalStackContainerConfig()
            .withBuckets("dev.drivewealth.mutualfunds2")
            .withServices(LocalStackContainer.Service.DYNAMODB)
        , new ActiveMQContainerConfig()
    );
  }

  public MutualFundsContainerSetup withKafkaUI(){
    withConfigs(new KafkaUIContainerConfig());
    return this;
  }

  @Override
  protected void setRootLogger(Logger root) {
    ((ch.qos.logback.classic.Logger)root).setLevel(Level.INFO);
  }

  public static void main(String[] args){
    new MutualFundsContainerSetup(true)
        .withKafkaUI()
        .launch(args);
  }
}
