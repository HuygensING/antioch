package nl.knaw.huygens.antioch.api.model;

/*
 * #%L
 * antioch-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Singleton;

import com.google.common.collect.Maps;

@Singleton
public class ProcessStatusMap<T extends ProcessStatus> {
  private final Map<UUID, T> map = Maps.newHashMap();

  public void put(UUID id, T status) {
    removeExpiredTasks();
    map.put(id, status);
  }

  public Optional<T> get(UUID id) {
    removeExpiredTasks();
    return Optional.ofNullable(map.get(id));
  }

  private void removeExpiredTasks() {
    List<UUID> expiredEntries = map.keySet().stream()//
        .filter(uuid -> map.get(uuid).isExpired())//
        .collect(toList());
    expiredEntries.forEach(map::remove);
  }

}
