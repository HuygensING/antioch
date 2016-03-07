package nl.knaw.huygens.alexandria.textlocator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;

import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextLocatorFactoryTest extends AlexandriaTest {

  private AlexandriaService service = Mockito.mock(AlexandriaService.class);
  private TextLocatorFactory textLocatorFactory = new TextLocatorFactory(service);

  @Test
  public void testInPrefixReturnsByIdTextLocator() throws TextLocatorParseException {
    AlexandriaTextLocator locator = textLocatorFactory.fromString("id:12b");
    softly.assertThat(locator).isInstanceOf(ByIdTextLocator.class);
    ByIdTextLocator byIdLocator = (ByIdTextLocator) locator;
    softly.assertThat(byIdLocator.getId()).isEqualTo("12b");
  }

  @Test
  public void testOffsetPrefixReturnsByOffsetTextLocator() throws TextLocatorParseException {
    AlexandriaTextLocator locator = textLocatorFactory.fromString("offset:1,3");
    softly.assertThat(locator).isInstanceOf(ByOffsetTextLocator.class);
    ByOffsetTextLocator byOffsetLocator = (ByOffsetTextLocator) locator;
    softly.assertThat(byOffsetLocator.getStart()).isEqualTo(1);
    softly.assertThat(byOffsetLocator.getLength()).isEqualTo(3);
  }

  @Test
  public void testUnknownPrefixThrowsException() {
    try {
      @SuppressWarnings("unused")
      AlexandriaTextLocator locator = textLocatorFactory.fromString("xid:12b");
      fail();
    } catch (TextLocatorParseException e) {
      assertThat(e.getMessage()).isEqualTo("The locator prefix 'xid' is not a valid prefix. Valid prefixes: [offset, id].");
    }
  }

}
