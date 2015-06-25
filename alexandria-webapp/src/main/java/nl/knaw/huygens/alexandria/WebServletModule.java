package nl.knaw.huygens.alexandria;

import javax.ws.rs.container.ResourceContext;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.JerseyResourceContext;

import com.squarespace.jersey2.guice.BootstrapUtils;

public class WebServletModule extends AlexandriaServletModule {

  @Override
  protected void configureServlets() {
    super.configureServlets();
    AlexandriaApplication rc = new AlexandriaApplication();
    for (Class<?> resource : rc.getClasses()) {
      Log.info("resourceClass={}", resource);
      bind(resource).asEagerSingleton();
    }

    bind(ServiceLocator.class).toInstance(createServiceLocator());
    bind(ResourceContext.class).to(JerseyResourceContext.class);
  }

  private ServiceLocator createServiceLocator() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    // BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule()));
    BootstrapUtils.install(locator);
    return locator;
  }

}
