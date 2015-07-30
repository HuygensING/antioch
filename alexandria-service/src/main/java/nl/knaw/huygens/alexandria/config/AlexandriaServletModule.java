package nl.knaw.huygens.alexandria.config;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.storage.PersistentTinkerGraphService;
import nl.knaw.huygens.alexandria.storage.TinkerPopService;

public class AlexandriaServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    // guice binds here
    Log.trace("configureServlets(): setting up Guice bindings");
    bind(AlexandriaService.class).to(PersistentTinkerGraphService.class);
    bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
    bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
    bind(TinkerPopService.class).to(PersistentTinkerGraphService.class);
    super.configureServlets();
  }
}
