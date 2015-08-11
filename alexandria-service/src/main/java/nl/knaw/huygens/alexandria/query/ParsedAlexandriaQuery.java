package nl.knaw.huygens.alexandria.query;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;

import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

public class ParsedAlexandriaQuery {
  private Class<? extends AlexandriaVF> vfClazz;
  private List<String> returnFields;
  private Predicate<Traverser<AnnotationVF>> predicate;
  private Comparator<AnnotationVF> comparator;
  private Function<AnnotationVF, Map<String, Object>> mapper;

  public void setVFClass(Class<? extends AlexandriaVF> vfClass) {
    this.vfClazz = vfClass;
  }

  public Class<? extends AlexandriaVF> getVFClass() {
    return this.vfClazz;
  }

  public void setReturnFields(List<String> returnFields) {
    this.returnFields = returnFields;
  }

  public List<String> getReturnFields() {
    return returnFields;
  }

  public void setResultMapper(Function<AnnotationVF, Map<String, Object>> mapper) {
    this.mapper = mapper;
  }

  public Function<AnnotationVF, Map<String, Object>> getResultMapper() {
    return mapper;
  }

  public void setPredicate(Predicate<Traverser<AnnotationVF>> predicate) {
    this.predicate = predicate;
  }

  public Predicate<Traverser<AnnotationVF>> getPredicate() {
    return predicate;
  }

  public void setResultComparator(Comparator<AnnotationVF> comparator) {
    this.comparator = comparator;
  }

  public Comparator<AnnotationVF> getResultComparator() {
    return comparator;
  }

}
