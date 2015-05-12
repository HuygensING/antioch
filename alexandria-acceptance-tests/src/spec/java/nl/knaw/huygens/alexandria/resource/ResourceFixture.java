package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ConcordionRunner.class)
public class ResourceFixture extends ApiFixture {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceFixture.class);

  private static final AlexandriaService RESOURCE_SERVICE_MOCK = mock(AlexandriaService.class);

  @BeforeClass
  public static void setup() {
    setupJerseyAndGuice(resourceModule());
    register(ResourcesEndpoint.class);
  }

  private static Module resourceModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        LOG.trace("setting up Guice bindings");
        bind(AlexandriaService.class).toInstance(RESOURCE_SERVICE_MOCK);
        bind(ResourceCreationRequestBuilder.class).toInstance(new ResourceCreationRequestBuilder());
      }
    };
  }

  @Override
  public void clear() {
    LOG.trace("clear");
    super.clear();
    Mockito.reset(RESOURCE_SERVICE_MOCK);
  }

  protected AlexandriaService resourceService() {
    return RESOURCE_SERVICE_MOCK;
  }
}
