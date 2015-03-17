package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.exception.MissingEntityException;
import nl.knaw.huygens.alexandria.util.IdentityCompatibilityArbiter;

@Consumes(MediaType.APPLICATION_JSON) // rfc4627: JSON text SHALL be encoded in Unicode. The default encoding is UTF-8.
@Produces(MediaType.APPLICATION_JSON)
public abstract class JSONEndpoint {
  protected void requireValidEntity(Object entity) {
    if (entity == null) {
      throw new MissingEntityException();
    }

    // versus: Optional.ofNullable(entity).orElseThrow(MissingEntityException::new);
  }

  protected void requireCompatibleIds(UUIDParam paramId, Supplier<UUID> identifiable) {
    IdentityCompatibilityArbiter.of(paramId.getValue()).requireCompatibleIdIn(identifiable);
  }
}
