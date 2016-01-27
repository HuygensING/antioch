package nl.knaw.huygens.alexandria.textlocator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class TextLocatorFactoryTest {

  private AlexandriaService service = Mockito.mock(AlexandriaService.class);
  private TextLocatorFactory textLocatorFactory = new TextLocatorFactory(service);

  @Test
  public void testInPrefixReturnsByIdTextLocator() throws TextLocatorParseException {
    AlexandriaTextLocator locator = textLocatorFactory.fromString("id:12b");
    assertThat(locator).isInstanceOf(ByIdTextLocator.class);
    ByIdTextLocator byIdLocator = (ByIdTextLocator) locator;
    assertThat(byIdLocator.getId()).isEqualTo("12b");
  }

  @Test
  public void testUnknownPrefixThrowsException() {
    try {
      @SuppressWarnings("unused")
      AlexandriaTextLocator locator = textLocatorFactory.fromString("xid:12b");
      fail();
    } catch (TextLocatorParseException e) {
      assertThat(e.getMessage()).isEqualTo("The locator prefix 'xid' is not a valid prefix. Valid prefix: 'id'.");
    }
  }

}
