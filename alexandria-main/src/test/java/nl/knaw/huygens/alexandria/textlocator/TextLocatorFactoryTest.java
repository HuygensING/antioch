package nl.knaw.huygens.alexandria.textlocator;

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
      assertThat(e.getMessage()).isEqualTo("The locator prefix 'xid' is not a valid prefix. Valid prefixes: [xpath, offset, id].");
    }
  }

}
