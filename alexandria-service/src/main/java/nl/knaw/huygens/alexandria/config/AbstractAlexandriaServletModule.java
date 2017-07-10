package nl.knaw.huygens.alexandria.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * #%L
 * alexandria-service
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

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.util.Scheduler;

public abstract class AbstractAlexandriaServletModule extends ServletModule {
  private static final int NTHREADS = 5;

  @Override
  protected void configureServlets() {
    // guice binds here
    // Log.trace("configureServlets(): setting up Guice bindings");
    Class<? extends TinkerPopService> tinkerpopServiceClass = getTinkerPopServiceClass();
    bind(AlexandriaService.class).to(tinkerpopServiceClass);
    bind(TinkerPopService.class).to(tinkerpopServiceClass);
    bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
    bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
    bind(Scheduler.class).in(Scopes.SINGLETON);
    ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
    bind(ExecutorService.class).toInstance(executorService);
    super.configureServlets();
  }

  abstract public Class<? extends TinkerPopService> getTinkerPopServiceClass();
}
