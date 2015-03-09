package nl.knaw.huygens.alexandria.external;

import static org.mockito.Mockito.mock;

import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.resource.Resources;
import nl.knaw.huygens.alexandria.service.ReferenceService;
import org.junit.BeforeClass;

public class ResourcesFixture extends ApiFixture {
  private static final ReferenceService referenceServiceMock = mock(ReferenceService.class);

  @BeforeClass
  public static void setup() {
    System.err.println("ResourcesFixture::setup");

    addClass(Resources.class);
    addProviderForContext(ReferenceService.class, referenceServiceMock);
  }

  protected ReferenceService referenceService() {
    return referenceServiceMock;
  }
}
