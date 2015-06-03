package nl.knaw.huygens.alexandria.config;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.ext.ContextResolver;

import nl.knaw.huygens.Log;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

  private final ResourceContext resourceContext;

  @Inject
  public ValidationConfigurationContextResolver(ResourceContext resourceContext) {
    Log.trace("resourceContext=[{}]", resourceContext);
    this.resourceContext = resourceContext;
  }

  @Override
  public ValidationConfig getContext(Class<?> type) {
    Log.trace("getContext: type=[{}]", type);
    return new ValidationConfig() //
        // .messageInterpolator(new ResourceBundleMessageInterpolator(//
        // new PlatformResourceBundleLocator("ValidationMessages")))
        .constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
  }

}
