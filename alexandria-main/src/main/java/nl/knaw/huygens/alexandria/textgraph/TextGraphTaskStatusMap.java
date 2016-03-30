package nl.knaw.huygens.alexandria.textgraph;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Maps;

public class TextGraphTaskStatusMap {
  private static Map<UUID, TextGraphImportStatus> map = Maps.newHashMap();

  public void put(UUID id, TextGraphImportStatus status) {
    removeExpiredTasks();
    map.put(id, status);
  }

  public Optional<TextGraphImportStatus> get(UUID id) {
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
