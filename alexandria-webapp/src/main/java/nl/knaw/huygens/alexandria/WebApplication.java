package nl.knaw.huygens.alexandria;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

public class WebApplication extends AlexandriaApplication {
  @Inject
  public WebApplication(ServiceLocator serviceLocator) {
    super();

    GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

    GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
    guiceBridge.bridgeGuiceInjector(ContextListener.injector);
  }
}
