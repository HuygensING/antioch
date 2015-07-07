package nl.knaw.huygens.alexandria;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.api.ServiceLocator;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

@ApplicationPath("/")
public class WebApplication extends AlexandriaApplication {
  public WebApplication() {
    super();
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    List<Module> modules = Arrays.asList(new AlexandriaServletModule());
    Injector injector = BootstrapUtils.newInjector(locator, modules);
    Log.debug("injector={}", injector);
    BootstrapUtils.install(locator);
  }
}
