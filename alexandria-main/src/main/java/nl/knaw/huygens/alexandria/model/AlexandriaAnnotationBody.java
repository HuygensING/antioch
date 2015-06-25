package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaAnnotationBody extends AbstractAccountable {
  private final String type;
  private final String value;

  public AlexandriaAnnotationBody(UUID id, String type, String value, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

}
