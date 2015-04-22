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
public class HuygensObjectMapperProvider implements ContextResolver<ObjectMapper> {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensObjectMapperProvider.class);

  private final ObjectMapper defaultObjectMapper;

  public HuygensObjectMapperProvider() {
    defaultObjectMapper = createDefaultMapper();
  }

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    LOG.trace("returning Jackson ObjectMapper for {}", type);
    return defaultObjectMapper;
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    LOG.debug("setting up Jackson ObjectMapper: [" + mapper + "]");

    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);

    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    return mapper;
  }
}
