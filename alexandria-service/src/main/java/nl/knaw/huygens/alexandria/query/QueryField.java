package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

enum QueryField {
  id(AnnotationVF::getUuid), //
  url(AlexandriaQueryParser::getAnnotationURL), //
  when(AnnotationVF::getProvenanceWhen), //
  who(AnnotationVF::getProvenanceWho), //
  why(AnnotationVF::getProvenanceWhy), //
  type(AnnotationVF::getType), //
  value(AnnotationVF::getValue), //
  state(AnnotationVF::getState), //
  resource_id(AnnotationVF::getResourceId), //
  subresource_id(AnnotationVF::getSubResourceId), //
  resource_url(AlexandriaQueryParser::getResourceURL), //
  subresource_url(AlexandriaQueryParser::getSubResourceURL);

  static final ImmutableList<QueryField> RESOURCE_FIELDS = ImmutableList.of(//
      resource_id, resource_url, //
      subresource_id, subresource_url//
  );

  static final List<String> ALL_EXTERNAL_NAMES = Arrays.stream(values())//
      .map(QueryField::externalName)//
      .collect(toList());

  Function<AnnotationVF, Object> getter;

  QueryField(Function<AnnotationVF, Object> getter) {
    this.getter = getter;
  }

  public String externalName() {
    return name().replace('_', '.');
  }

  public static QueryField fromExternalName(String externalName) {
    return valueOf(externalName.replace('.', '_'));
  }

}
