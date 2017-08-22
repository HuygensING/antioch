package nl.knaw.huygens.alexandria.config;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {
  private final ResourceContext resourceContext;

  @Inject
  public ValidationConfigurationContextResolver(ResourceContext resourceContext) {
    this.resourceContext = resourceContext;
  }

  @Override
  public ValidationConfig getContext(Class<?> type) {
    return new ValidationConfig() //
        // .messageInterpolator(new ResourceBundleMessageInterpolator(//
        // new PlatformResourceBundleLocator("ValidationMessages")))
        .constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
  }

}
