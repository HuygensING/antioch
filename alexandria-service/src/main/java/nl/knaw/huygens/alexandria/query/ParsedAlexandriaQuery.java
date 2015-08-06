package nl.knaw.huygens.alexandria.query;

import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;

public class ParsedAlexandriaQuery {
  private Class<? extends AlexandriaVF> vfClazz;

  public Class<? extends AlexandriaVF> getVFClass() {
    return this.vfClazz;
  }

  public void setVfClazz(Class<? extends AlexandriaVF> vfClazz) {
    this.vfClazz = vfClazz;
  }

}
