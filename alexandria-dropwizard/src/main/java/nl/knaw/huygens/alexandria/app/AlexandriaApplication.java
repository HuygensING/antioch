package nl.knaw.huygens.alexandria.app;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.alexandria.endpoint.homepage.HomePageEndpoint;
import nl.knaw.huygens.alexandria.health.AboutHealthCheck;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TitanService;

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
    // AlexandriaServletModule alexandriaServletModule = new AlexandriaServletModule();
    // GuiceBundle<AlexandriaAppConfiguration> guiceBundle = GuiceBundle.<AlexandriaAppConfiguration> newBuilder()//
    // .addModule(new ConfigModule())//
    // .addModule(alexandriaServletModule)//
    // .enableAutoConfig(getClass().getPackage().getName())//
    // .setConfigClass(AlexandriaAppConfiguration.class)//
    // .build();
    //
    // bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(AlexandriaAppConfiguration config, Environment environment) throws Exception {
    // Properties gitProperties = new Properties();
    // gitProperties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
    // final String version = gitProperties.getProperty("git.commit.id");
    // Log.info("Launching Alexandria version: {}", version);

    environment.healthChecks().register("about", new AboutHealthCheck());

    LocationBuilder locationBuilder = new LocationBuilder(config, new EndpointPathResolver());
    AlexandriaService service = new TitanService(locationBuilder, config);

    environment.jersey().packages(JSONEndpoint.class.getPackage().getName());
    environment.jersey().register(new AboutEndpoint(config, service));
    environment.jersey().register(new HomePageEndpoint());
    // environment.jersey().register(new LoggingFilter());
    // environment.jersey().register(new JacksonJsonProvider());
  }

  static class ConfigModule implements Module {

    @Override
    public void configure(Binder binder) {
      binder.bind(AlexandriaConfiguration.class).to(AlexandriaAppConfiguration.class);
    }

  }
}
