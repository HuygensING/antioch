package nl.knaw.huygens.alexandria.api.model;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Singleton;

import com.google.common.collect.Maps;

@Singleton
public class ProcessStatusMap<T extends ProcessStatus> {
  private Map<UUID, T> map = Maps.newHashMap();

  public void put(UUID id, T status) {
    removeExpiredTasks();
    map.put(id, status);
  }

  public Optional<T> get(UUID id) {
    removeExpiredTasks();
    return Optional.ofNullable(map.get(id));
  }

  public void removeExpiredTasks() {
    List<UUID> expiredEntries = map.keySet().stream()//
        .filter(uuid -> map.get(uuid).isExpired())//
        .collect(toList());
    expiredEntries.forEach(key -> map.remove(key));
  }

}
