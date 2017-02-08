package nl.knaw.huygens.alexandria.endpoint.webannotation;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class AlexandriaResourceCache {
  static Cache<String, Optional<AlexandriaResource>> cache = CacheBuilder.newBuilder()//
      .maximumSize(1000)//
      .build();

  public void add(AlexandriaResource resource) {
    cache.put(resource.getCargo(), Optional.of(resource));
  }

  static final Callable<Optional<AlexandriaResource>> EMPTY_WHEN_MISSING = Optional::empty;

  public Optional<AlexandriaResource> get(String cargo) {
    Optional<AlexandriaResource> oResource = Optional.empty();
    try {
      oResource = cache.get(cargo, EMPTY_WHEN_MISSING);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return oResource;
  }

}
