package nl.knaw.huygens.alexandria.config;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class JsonConfiguration implements ContextResolver<ObjectMapper> {
  private static final Logger LOG = LoggerFactory.getLogger(JsonConfiguration.class);

  private final ObjectMapper defaultObjectMapper;

  public JsonConfiguration() {
    defaultObjectMapper = createDefaultMapper();
  }

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    LOG.trace("Returning Jackson ObjectMapper for type: {}", type);
    return defaultObjectMapper;
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    LOG.debug("Configuring Jackson ObjectMapper: [" + mapper + "]");

    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);

    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    return mapper;
  }
}
