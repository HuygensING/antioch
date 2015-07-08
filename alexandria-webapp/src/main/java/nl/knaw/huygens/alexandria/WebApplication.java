package nl.knaw.huygens.alexandria;

import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.api.ServiceLocator;

import com.google.inject.Injector;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

@ApplicationPath("/")
public class WebApplication extends AlexandriaApplication {
  public WebApplication() {
    super();
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Injector injector = BootstrapUtils.newInjector(locator, ContextListener.MODULES);
    Log.debug("injector={}", injector);
    BootstrapUtils.install(locator);
  }
}
