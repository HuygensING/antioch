package nl.knaw.huygens.alexandria.external;

import static org.mockito.Mockito.mock;

import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.resource.Resources;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.junit.BeforeClass;

public class ResourcesFixture extends ApiFixture {
  private static final ResourceService RESOURCE_SERVICE_MOCK = mock(ResourceService.class);

  @BeforeClass
  public static void setup() {
    addClass(Resources.class);
    addProviderForContext(ResourceService.class, RESOURCE_SERVICE_MOCK);
  }

  protected ResourceService resourceService() {
    return RESOURCE_SERVICE_MOCK;
  }
}
