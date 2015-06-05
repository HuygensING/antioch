package nl.knaw.huygens.alexandria.frames;

import java.util.UUID;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.Labels;

public class AlexandriaResourceVF extends AbstractVertexFrame {

  public static final Labels LABEL = Labels.Resource;

  enum Properties {
    uuid, ref, state, //
    provenanceWho, provenanceWhen, provenanceWhy
  }

  public UUID getUUID() {
    return UUID.fromString(getProperty(Properties.uuid.name()));
  }

  public void setUUID(UUID uuid) {
    setProperty(Properties.uuid.name(), uuid.toString());
  }

  public String getRef() {
    return getProperty(Properties.ref.name());
  }

  public void setRef(String ref) {
    setProperty(Properties.ref.name(), ref);
  }

  public AlexandriaState getState() {
    return AlexandriaState.valueOf(getProperty(Properties.state.name()));
  }

  public void setState(AlexandriaState state) {
    setProperty(Properties.state.name(), state.name());
  }

  public TentativeAlexandriaProvenance getProvenance() {
    return new TentativeAlexandriaProvenance(//
        getProperty(Properties.provenanceWho.name()),//
        getProperty(Properties.provenanceWhen.name()),//
        getProperty(Properties.provenanceWhy.name())//
    );
  }

  public void setProvenance(AlexandriaProvenance provenance) {
    setProperty(Properties.provenanceWhen.name(), provenance.getWhen());
    setProperty(Properties.provenanceWho.name(), provenance.getWho());
    setProperty(Properties.provenanceWhy.name(), provenance.getWhy());
  }

  // public void addAnnotation(AlexandriaAnnotation annotation) {
  // r.addAnnotation(annotation);
  // }
  //
  // public Set<AlexandriaAnnotation> getAnnotations() {
  // return r.getAnnotations();
  // }

  public static AlexandriaResourceVF of(AlexandriaResource resource) {
    AlexandriaResourceVF vf = new AlexandriaResourceVF();
    vf.setUUID(resource.getId());
    vf.setProvenance(resource.getProvenance());
    vf.setRef(resource.getRef());
    vf.setState(resource.getState());
    return vf;
  }

  public AlexandriaResource get() {
    AlexandriaResource ar = new AlexandriaResource(getUUID(), getProvenance());
    ar.setRef(getRef());
    ar.setState(getState());
    return ar;
  }

}
