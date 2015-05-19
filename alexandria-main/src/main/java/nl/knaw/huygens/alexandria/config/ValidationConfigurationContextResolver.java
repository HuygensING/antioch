package nl.knaw.huygens.alexandria.config;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationConfigurationContextResolver.class);

  private final ResourceContext resourceContext;

  @Inject
  public ValidationConfigurationContextResolver(ResourceContext resourceContext) {
    LOG.trace("resourceContext=[{}]", resourceContext);
    this.resourceContext = resourceContext;
  }

  @Override
  public ValidationConfig getContext(Class<?> type) {
    LOG.trace("getContext: type=[{}]", type);
    return new ValidationConfig() //
//        .messageInterpolator(new ResourceBundleMessageInterpolator(//
//            new PlatformResourceBundleLocator("ValidationMessages")))
        .constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
  }

}
