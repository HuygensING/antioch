package nl.knaw.huygens.alexandria.util;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.fasterxml.jackson.datatype.joda.JodaModule;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
  private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperProvider.class);
  private static final ObjectMapper OBJECT_MAPPER = objectMapper();

  @Override
  public ObjectMapper getContext(Class<?> type) {
    // Let's find out if singleton is good enough. If it isn't, just return objectMapper();
    return OBJECT_MAPPER;
  }

  private static ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    LOG.debug("Setting up Jackson ObjectMapper: [" + mapper + "]");

    // These are 'dev' settings giving us human readable output.
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // JodaModule maps DateTime to a flat String (or timestamp, see above) instead of recursively yielding
    // the entire object hierarchy of DateTime which is way too verbose.
//    mapper.registerModule(new JodaModule());

    return mapper;
  }
}
