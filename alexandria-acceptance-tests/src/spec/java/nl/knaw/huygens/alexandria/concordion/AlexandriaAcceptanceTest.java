package nl.knaw.huygens.alexandria.concordion;

/*
 * #%L
 * alexandria-acceptance-tests
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

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.Identifiable;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerGraphService;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.cat.RestExtension;
import nl.knaw.huygens.cat.RestFixture;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.junit.BeforeClass;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static nl.knaw.huygens.alexandria.api.model.AlexandriaState.CONFIRMED;

public class AlexandriaAcceptanceTest extends RestFixture {
  private static final AlexandriaConfiguration CONFIG = testConfiguration();

  private static Storage storage = tinkerGraphStorage();

  private static LocationBuilder locationBuilder = new LocationBuilder(testConfiguration(), new EndpointPathResolver());

  private static TinkerPopService service = new TinkerPopService(storage, locationBuilder);

  private final AtomicInteger nextUniqueExpressionNumber = new AtomicInteger();

  @Extension
  public RestExtension extensionFoundViaReflection = new RestExtension().enableCodeMirror().includeBootstrap();

  @Extension
  public ConcordionExtension imagesExtension = concordionExtender //
  -> concordionExtender.withResource("/tcc.svg", new Resource("/nl/knaw/huygens/alexandria/transactions/tcc.svg"));

  @BeforeClass
  public static void setupAlexandriaAcceptanceTest() {
    Log.debug("Setting up AlexandriaAcceptanceTest");
    setupRestFixture(alexandriaModule());
    register(JsonConfiguration.class);
  }

  private static Storage tinkerGraphStorage() {
    return new Storage(TinkerGraph.open());
  }

  protected static AlexandriaConfiguration testConfiguration() {
    return new AlexandriaConfiguration() {
      @Override
      public URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(4242).build();
      }

      @Override
      public String getStorageDirectory() {
        return "/tmp";
      }

      @Override
      public String toString() {
        return Objects.toStringHelper(this).add("baseURI", getBaseURI()).toString();
      }

      @Override
      public Map<String, String> getAuthKeyIndex() {
        return ImmutableMap.of("123456", "testuser");
      }

      @Override
      public String getAdminKey() {
        return "whatever";
      }

      @Override
      public Boolean asynchronousEndpointsAllowed() {
        return false;
      }
    };
  }

  private static Module alexandriaModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
        bind(TinkerPopService.class).to(TinkerGraphService.class);
        bind(AlexandriaService.class).toInstance(service);
        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
        bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
        bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
        bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        bind(ExecutorService.class).toInstance(executorService);
      }
    };
  }

  @Override
  public void clear() {
    Log.debug("Clearing {}", getClass().getSimpleName());
    super.clear();
  }

  @Override
  protected Application configure() {
    return super.configure(); // maybe move enabling of LOG_TRAFFIC and DUMP_ENTITY from RestFixture here?
  }

  public String uuid() {
    return Iterables.getLast(Splitter.on('/').split(location().orElse("(not set)")));
  }


  public void clearStorage() {
    Log.debug("Clearing Storage");
    service.setStorage(tinkerGraphStorage());
  }

  public void wait5seconds() {
    try {
      Thread.sleep(5000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void resourceExists(String resId) {
    resourceExists(resId, aRef());
  }

  public void resourceExists(String resId, String ref) {
    service.createOrUpdateResource(fromString(resId), ref, aProvenance(), CONFIRMED);
  }

  public String hasSubresource(String parentUUID) {
    return hasSubresource(parentUUID, aSub());
  }

  public String hasSubresource(String parentUUID, String sub) {
    final UUID subId = service().createSubResource(randomUUID(), fromString(parentUUID), sub, aProvenance()).getId();
    service().confirmResource(subId);
    return subId.toString();
  }

  // TODO: move to markup module
  // public void resourceHasText(String resId, String xml) {
  // ParseResult result = TextGraphUtil.parse(xml);
  // service().storeTextGraph(UUID.fromString(resId), result);
  // }
  //
  // public void resourceHasAnnotator(String resId, String code, String description) {
  // service().setResourceAnnotator(UUID.fromString(resId), anAnnotator(code, description));
  // }
  //
  // private Annotator anAnnotator(String code, String description) {
  // return new Annotator().setCode(code).setDescription(description);
  // }

  protected AlexandriaService service() {
    return service;
  }

  protected String idOf(Identifiable it) {
    return it.getId().toString();
  }

  protected AlexandriaResource theResource(UUID resId) {
    return service().readResource(resId).get();
  }

  protected String annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationBody, TentativeAlexandriaProvenance provenance) {
    return idOf(service.annotate(resource, annotationBody, provenance));
  }

  protected TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("nederlab", Instant.now(), "details warranting this object's existence");
  }

  protected String aRef() {
    return "http://www.example.com/some/ref";
  }

  protected String aSub() {
    return "/some/folia/expression/" + nextUniqueExpressionNumber.getAndIncrement();
  }

  protected UUID hasConfirmedAnnotation(AlexandriaResource resource, AlexandriaAnnotationBody annotationBody) {
    return hasConfirmedAnnotation(resource, annotationBody, aProvenance());
  }

  protected UUID hasConfirmedAnnotation(AlexandriaResource resource, AlexandriaAnnotationBody annotationBody, TentativeAlexandriaProvenance provenance) {
    final UUID annotationId = service().annotate(resource, annotationBody, provenance).getId();
    service().confirmAnnotation(annotationId);
    return annotationId;
  }

  protected AlexandriaAnnotationBody anAnnotation() {
    return anAnnotation("t", "v");
  }

  protected AlexandriaAnnotationBody anAnnotation(String type, String value) {
    return anAnnotation(type, value, aProvenance());
  }

  protected AlexandriaAnnotationBody anAnnotation(String type, String value, TentativeAlexandriaProvenance provenance) {
    return service().createAnnotationBody(randomUUID(), type, value, provenance);
  }

}
