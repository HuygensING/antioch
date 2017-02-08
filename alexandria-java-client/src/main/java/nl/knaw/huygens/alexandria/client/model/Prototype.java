package nl.knaw.huygens.alexandria.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonInclude(Include.NON_ABSENT)
public abstract class Prototype extends JsonWrapperObject {
}
