package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-service
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class InMemoryTextServiceTest {

  @Test
  public void whatGoesInMustComeOut() throws IOException {
    String content = "What goes in";
    InMemoryTextService imts = new InMemoryTextService();
    UUID resourceUUID = UUID.randomUUID();
    InputStream stream = IOUtils.toInputStream(content);
    imts.setFromStream(resourceUUID, stream);
    Optional<InputStream> optionalStream = imts.getAsStream(resourceUUID);
    assertThat(optionalStream).isPresent();
    String out = IOUtils.toString(optionalStream.get());
    assertThat(out).isEqualTo(content);
  }

}
