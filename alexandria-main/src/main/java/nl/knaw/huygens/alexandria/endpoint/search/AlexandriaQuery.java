package nl.knaw.huygens.alexandria.endpoint.search;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;

@JsonTypeName("query")
public class AlexandriaQuery extends JsonWrapperObject implements Prototype {
  String command = "";
}
