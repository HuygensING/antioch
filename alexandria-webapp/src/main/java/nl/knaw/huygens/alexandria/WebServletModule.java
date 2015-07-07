package nl.knaw.huygens.alexandria;

import org.glassfish.jersey.servlet.ServletContainer;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

public class WebServletModule extends AlexandriaServletModule {

  @Override
  protected void configureServlets() {
    super.configureServlets();
    AlexandriaApplication rc = new AlexandriaApplication();
    for (Class<?> resource : rc.getClasses()) {
      Log.info("resourceClass={}", resource);
      bind(resource).asEagerSingleton();
    }
    ServletContainer instance = new ServletContainer(rc);
    bind(ServletContainer.class).toInstance(instance);
    // bind(ResourceContext.class).to(JerseyResourceContext.class);
    serve("/api/*").with(MyServlet.class);
    serve("/v1/*").with(instance);
  }

}
