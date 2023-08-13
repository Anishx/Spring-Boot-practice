package com.drivewealth.testing.containers;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @deprecated Bring over nick's java based SFTP class
 */
public class SFTPContainer extends GenericContainer< SFTPContainer > implements ITestContainerAware {
  private Logger logger = LoggerFactory.getLogger( getClass() );
  private File sftpHomeDirectory;
  public SFTPContainer(String image  ) {
    super(image);
    try {
      sftpHomeDirectory = Files.createTempDirectory( "sftp-home" ).toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    withExposedPorts( 22 );
    withClasspathResourceMapping( "sftp-start.sh", "/start_sftp.sh", BindMode.READ_ONLY );
    withClasspathResourceMapping( "sftp-users.conf", "/etc/sftp/users.conf", BindMode.READ_ONLY );
    withFileSystemBind( sftpHomeDirectory.getAbsolutePath(), "/home/sftp", BindMode.READ_WRITE);
    withCommand( "bash start_sftp.sh" );


  }

  public String getUser(){
    return "sftp";
  }
  public String getPassword(){
    return "sftp";
  }

  @Override
  public void start() {
    logger.info( "User Home:{} ", sftpHomeDirectory.getAbsolutePath() );
    super.start();
  }

  @Override
  public void stop() {
//    openFiles();
    super.stop();
  }

  public void openFiles(){
    exec( this, "chmod -R 777 /sftp");
  }

  public void mkdir( String... directories ) {
    File home = getSftpHome();
    for ( String dir : directories ) {
      logger.info( "Creating Directory in SFTP home: {}", dir );
      exec( this, "mkdir -p /home/sftp/sftp/" + dir  );
    }
    exec( this, "chown -R sftp.users /home/sftp/sftp" );
  }

  @NotNull
  private File getSftpHome() {
    File home = new File( sftpHomeDirectory, "sftp" );
    return home;
  }


  public File createFile( String path, String contents ){
    File file = new File( getSftpHome(), path );
    try {
      openFiles();
      if ( file.getParentFile().exists() == false && file.getParentFile().mkdirs() == false) {
        throw new IllegalArgumentException( "Failed to create housing directory: " + file.getParentFile().getAbsolutePath() );
      }
      FileUtils.writeStringToFile( file, contents, "UTF8" );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return file;
  }

}
