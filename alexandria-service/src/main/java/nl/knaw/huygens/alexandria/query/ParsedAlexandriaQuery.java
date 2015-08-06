package nl.knaw.huygens.alexandria.query;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;

public class ParsedAlexandriaQuery {
  private Class<? extends AlexandriaVF> vfClazz;
  private List<String> returnFields;

  public Class<? extends AlexandriaVF> getVFClass() {
    return this.vfClazz;
  }

  public void setVfClazz(Class<? extends AlexandriaVF> vfClazz) {
    this.vfClazz = vfClazz;
  }

  public void setReturnFields(List<String> returnFields) {
    this.returnFields = returnFields;
  }

  public List<String> getReturnFields() {
    return returnFields;
  }

}
