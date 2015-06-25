package nl.knaw.huygens.alexandria.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractAnnotatable extends AbstractAccountable {
  private AlexandriaState state = AlexandriaState.Default;
  private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

  protected AbstractAnnotatable(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(AlexandriaAnnotation annotation) {
    annotations.add(annotation);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    AccountablePointer<? extends Accountable> pointer = new AccountablePointer(this.getClass(), this.getId().toString());
    annotation.setAnnotatablePointer(pointer);
  }

  public AlexandriaState getState() {
    return state;
  }

  public void setState(AlexandriaState state) {
    this.state = state;
  }

}
