package nl.knaw.huygens.alexandria.storage;

import org.apache.tinkerpop.gremlin.structure.io.Io.Builder;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;

public enum DumpFormat {
  gryo(IoCore.gryo()), //
  graphml(IoCore.graphml()), //
  graphson(IoCore.graphson());

  public Builder<?> builder;

  DumpFormat(Builder<?> builder) {
    this.builder = builder;
  }

}