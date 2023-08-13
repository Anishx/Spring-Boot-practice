package com.drivewealth.testing.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public interface ITestContainerAware {
  default void exec (Container< ? > container, String command ) {
    Container.ExecResult result = null;
    try {
      result = container.execInContainer( "sh", "-c", command );
      Logger logger = LoggerFactory.getLogger( getClass() );
      if (StringUtils.isNotBlank( result.getStdout() ) ) {
        logger.info( result.getStdout() );
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    assertEquals( "Error encountered: "+result.getStderr(), 0, result.getExitCode() );
  }
}
