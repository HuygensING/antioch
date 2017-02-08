package nl.knaw.huygens.alexandria.jersey.providers;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class StreamBodyWriter implements MessageBodyWriter<Stream<?>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Stream.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Stream<?> stream, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Stream<?> stream, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    JsonGenerator jg = new JsonFactory().createGenerator(entityStream, JsonEncoding.UTF8);
    jg.writeStartArray();
    AtomicBoolean first = new AtomicBoolean(true);
    stream.forEach(i -> {
      try {
        String json = objectMapper.writeValueAsString(i);
        if (first.get()) {
          first.set(false);
        } else {
          jg.writeRaw(',');
        }
        jg.writeRaw(json);
        jg.flush();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    jg.writeEndArray();

    jg.flush();
    jg.close();

  }
}
