package nl.knaw.huygens.alexandria.textlocator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TextLocatorFactoryTest {

  @Test
  public void testInPrefixReturnsByIdTextLocator() throws TextLocatorParseException {
    AlexandriaTextLocator locator = TextLocatorFactory.fromString("id:12b");
    assertThat(locator).isInstanceOf(ByIdTextLocator.class);
    ByIdTextLocator byIdLocator = (ByIdTextLocator) locator;
    assertThat(byIdLocator.getId()).isEqualTo("12b");
  }

  @Test
  public void testUnknownPrefixThrowsException() {
    try {
      @SuppressWarnings("unused")
      AlexandriaTextLocator locator = TextLocatorFactory.fromString("xid:12b");
      fail();
    } catch (TextLocatorParseException e) {
      assertThat(e.getMessage()).isEqualTo("locator prefix 'xid' not recognized");
    }
  }

}
