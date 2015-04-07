package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON) // rfc4627: JSON text SHALL be encoded in Unicode. The default encoding is UTF-8.
@Produces(MediaType.APPLICATION_JSON)
public abstract class JSONEndpoint {
}
