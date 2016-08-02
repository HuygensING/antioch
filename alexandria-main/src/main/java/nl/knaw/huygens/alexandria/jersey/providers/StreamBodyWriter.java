package nl.knaw.huygens.alexandria.jersey.providers;

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