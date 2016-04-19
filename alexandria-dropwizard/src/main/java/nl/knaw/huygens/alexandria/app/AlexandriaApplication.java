package nl.knaw.huygens.alexandria.app;

import java.util.Properties;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.health.AboutHealthCheck;

public class AlexandriaApplication extends Application<AlexandriaAppConfiguration> {
  public static void main(String... args) throws Exception {
    new AlexandriaApplication().run(args);
  }

  @Override
  public String getName() {
    return "Alexandria";
  }

  @Override
  public void initialize(Bootstrap<AlexandriaAppConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle());
    bootstrap.addBundle(new Java8Bundle());
    // GuiceBundle<AlexandriaAppConfiguration> guiceBundle = GuiceBundle.<AlexandriaAppConfiguration> newBuilder()//
    // .addModule(new AlexandriaServletModule())//
    // .enableAutoConfig(getClass().getPackage().getName())//
    // .setConfigClass(AlexandriaAppConfiguration.class)//
    // .build();
    //
    // bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(AlexandriaAppConfiguration alexandriaConfiguration, Environment environment) throws Exception {
    Properties gitProperties = new Properties();
    gitProperties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
    final String version = gitProperties.getProperty("git.commit.id");
    Log.info("Launching Alexandria version: {}", version);

    Log.trace("config.name=[{}]", alexandriaConfiguration.getName());

    environment.healthChecks().register("about", new AboutHealthCheck());

    // environment.jersey().register(new AboutEndpoint());
    // environment.jersey().register(new LoggingFilter());
    // environment.jersey().register(new JacksonJsonProvider());
  }
}
