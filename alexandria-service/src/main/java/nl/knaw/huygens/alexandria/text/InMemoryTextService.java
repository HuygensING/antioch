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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.Log;

public class InMemoryTextService implements TextService {
  Map<UUID, String> texts = new HashMap<>();

  // @Override
  // public void set(UUID resourceUUID, String text) {
  // texts.put(resourceUUID, text);
  // }

  @Override
  public void setFromStream(UUID resourceUUID, InputStream stream) {
    try {
      texts.put(resourceUUID, IOUtils.toString(stream));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // @Override
  // public Optional<String> get(UUID resourceUUID) {
  // return Optional.ofNullable(texts.get(resourceUUID));
  // }

  @Override
  public Optional<InputStream> getAsStream(UUID resourceUUID) {
    Log.info("keys={}", texts.keySet());
    if (texts.containsKey(resourceUUID)) {
      try {
        InputStream in = IOUtils.toInputStream(texts.get(resourceUUID), "UTF-8");
        return Optional.of(in);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Optional.empty();
  }

}
