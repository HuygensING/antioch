package nl.knaw.huygens.antioch.jersey.providers;

/*
 * #%L
 * antioch-main
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
