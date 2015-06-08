package nl.knaw.huygens.alexandria.config;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerpopAlexandriaService;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class AlexandriaServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    // guice binds here
    Log.trace("setting up Guice bindings");
    bind(AlexandriaService.class).to(TinkerpopAlexandriaService.class);
    bind(AlexandriaConfiguration.class).to(TinkerpopAlexandriaConfiguration.class);
    bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
    bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);

    super.configureServlets();
  }
}
