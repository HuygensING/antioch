package nl.knaw.huygens.alexandria.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;

public class AnnotationService {
  private final Map<Integer, Anno> annotations = Maps.newHashMap();
  private final AtomicInteger nextUniqueId = new AtomicInteger(0);

  public AnnotationService() {
    createAnnotation("Emotion", "Happy");
  }

  public int createAnnotation(String key, String value) {
    Anno anno = new Anno(key, value);
    int id = nextUniqueId.incrementAndGet();
    annotations.put(id, anno);
    return id;
  }

  public Optional<Anno> getAnnotation(int id) {
    return Optional.ofNullable(annotations.get(id));
  }

  public static class Anno {
    private final String key;
    private final String value;

    public Anno(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }
    
    public String getValue() {
      return value;
    }
    
    @Override
    public String toString() {
      return String.format("Annotation[key=%s,value=%s]", key, value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null || obj.getClass() != getClass()) {
        return false;
      }
      Anno peer = (Anno) obj;
      return Objects.equals(this.key, peer.key) //
          && Objects.equals(this.value, peer.value);
    }
  }
}
