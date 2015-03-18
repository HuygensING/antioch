package nl.knaw.huygens.alexandria.util;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
  private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperProvider.class);
  private static final ObjectMapper OBJECT_MAPPER = objectMapper();

  @Override
  public ObjectMapper getContext(Class<?> type) {
    LOG.trace("returning Jackson ObjectMapper for {}", type);

    // Let's find out if singleton is good enough. If it isn't, just return objectMapper();
    return OBJECT_MAPPER;
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    LOG.debug("setting up Jackson ObjectMapper: [" + mapper + "]");

    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    // These are 'dev' settings giving us human readable output.
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);

    return mapper;
  }
}
