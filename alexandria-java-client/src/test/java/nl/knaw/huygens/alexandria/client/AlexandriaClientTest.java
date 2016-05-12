package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public abstract class AlexandriaClientTest extends AlexandriaTest {
  static final String AUTHKEY = "AUTHKEY";

  private static URI testURI = URI.create("http://localhost:2016/");
  private static HttpServer testServer;
  private static AlexandriaConfiguration testConfig = new AlexandriaConfiguration() {
    @Override
    public String getStorageDirectory() {
      return System.getProperty("java.io.tmpdir");
    }

    @Override
    public URI getBaseURI() {
      return testURI;
    }

    @Override
    public Map<String, String> getAuthKeyIndex() {
      return ImmutableMap.of("AUTHKEY", "testuser");
    }

    @Override
    public String getAdminKey() {
      return "adminkey";
    }

    @Override
    public Boolean asynchronousEndpointsAllowed() {
      return true;
    }
  };

  AlexandriaClient client = new AlexandriaClient(testURI);

  @BeforeClass
  public static void startTestServer() {
    final ServiceLocator locator = createServiceLocator();
    final AlexandriaService service = locator.getService(AlexandriaService.class);
    final ResourceConfig resourceConfig = new AlexandriaApplication();
    ((TinkerPopService) service).setStorage(new Storage(TinkerGraph.open()));
    testServer = GrizzlyHttpServerFactory.createHttpServer(testURI, resourceConfig, locator);
  }

  @AfterClass
  public static void stopTestServer() {
    testServer.shutdown();
  }

  void assertRequestSucceeded(RestResult<?> result) {
    assertThat(result).isNotNull();
    assertThat(result.hasFailed())//
        .as("Request went OK")//
        .withFailMessage("request failed: %s", result.getFailureCause().orElse("something you should never see"))//
        .isFalse();
  }

  protected UUID createResource(String resourceRef) {
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    UUID resourceUuid = UUID.randomUUID();
    RestResult<Void> result = client.setResource(resourceUuid, resource);
    assertRequestSucceeded(result);
    return resourceUuid;
  }

  protected TextImportStatus setResourceText(UUID resourceUuid, String xml) {
    RestResult<URI> result = client.setResourceText(resourceUuid, xml);
    assertThat(result).isNotNull();
    assertThat(result.hasFailed()).isFalse();

    TextImportStatus textGraphImportStatus = null;
    boolean goOn = true;
    while (goOn) {
      RestResult<TextImportStatus> result2 = client.getTextImportStatus(resourceUuid);
      assertThat(result2.hasFailed()).isFalse();
      textGraphImportStatus = result2.get();
      goOn = !textGraphImportStatus.isDone();
    }
    return textGraphImportStatus;
  }

  protected UUID annotateResource(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    RestResult<UUID> result = client.annotateResource(resourceUuid, annotationPrototype);
    assertRequestSucceeded(result);
    UUID annotationUuid = result.get();
    return annotationUuid;
  }

  private static ServiceLocator createServiceLocator() {
    final ServiceLocator locator = BootstrapUtils.newServiceLocator();
    final AbstractModule configModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(testConfig);
      }
    };
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
    BootstrapUtils.install(locator);
    return locator;
  }

}
