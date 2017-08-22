package nl.knaw.huygens.alexandria.api.model.search;

/*
 * #%L
 * alexandria-api
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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public enum QueryField {
  id, //
  url, //
  when, //
  who, //
  why, //
  type, //
  value, //
  state, //
  resource_id, //
  subresource_id, //
  resource_ref, //
  subresource_sub, //
  resource_url, //
  subresource_url;

  public static final ImmutableList<QueryField> RESOURCE_FIELDS = ImmutableList.of(//
      resource_id, resource_url, resource_ref, //
      subresource_id, subresource_url, subresource_sub//
  );

  public static final List<String> ALL_EXTERNAL_NAMES = Arrays.stream(values())//
      .map(QueryField::externalName)//
      .collect(toList());

  public String externalName() {
    return name().replace('_', '.');
  }

  public static QueryField fromExternalName(String externalName) {
    return valueOf(externalName.replace('.', '_'));
  }

}
