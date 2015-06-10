package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public interface Accountable {
  UUID getId();
  AlexandriaProvenance getProvenance();
}
