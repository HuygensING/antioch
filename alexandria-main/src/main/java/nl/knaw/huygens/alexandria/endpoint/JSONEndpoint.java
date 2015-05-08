package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Consumes(MediaType.APPLICATION_JSON) // rfc4627: JSON text SHALL be encoded in Unicode. The default encoding is UTF-8.
@Produces(MediaType.APPLICATION_JSON)
public abstract class JSONEndpoint {
  private static final String MISSING_RESOURCE_BODY_MESSAGE = "Missing or empty request body";

  private static final Logger LOG = LoggerFactory.getLogger(JSONEndpoint.class);

  protected void trace(String format, Object... arguments) {
    LOG.trace(format, arguments);
  }

  // TODO: we can now get rid of this by using Jersey 2 Bean Validation support! :-)
  protected Supplier<BadRequestException> missingBodyException() {
    return () -> badRequestException(MISSING_RESOURCE_BODY_MESSAGE);
  }

  protected BadRequestException badRequestException(String message) {
    LOG.trace(message);
    return new BadRequestException(message);
  }
}
