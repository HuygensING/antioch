package nl.knaw.huygens.alexandria.nederlab;

/*
 * #%L
 * alexandria-acceptance-tests
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.UUID.fromString;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.endpoint.search.SearchEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class NederlabFixture extends AlexandriaAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(SearchEndpoint.class);
  }

  public AlexandriaResource subResourceExists(String id, String parentId) {
    UUID resourceId = fromString(id);
    UUID parentUUID = fromString(parentId);
    return service().createSubResource(resourceId, parentUUID, aSub(), aProvenance());
  }

  public String resourceHasAnnotation(String id) {
    return resourceHasAnnotation(id, "type", "value");
  }

  public String resourceHasAnnotation(String resId, String type, String value) {
    final AlexandriaResource resource = theResource(fromString(resId));
    final AlexandriaAnnotationBody annotationBody = anAnnotation(type, value);
    return hasConfirmedAnnotation(resource, annotationBody).toString();
  }

  public String resourceExistsWithTagForUserAtInstant(String resId, String value, String who, String when) {
    resourceExists(resId);

    final AlexandriaResource resource = theResource(fromString(resId));

    final Instant whenAsInstant = ISO_DATE_TIME.parse(when, Instant::from);
    final TentativeAlexandriaProvenance provenance = aProvenance(who, whenAsInstant);
    final AlexandriaAnnotationBody annotationBody = anAnnotation("Tag", value, provenance);

    return hasConfirmedAnnotation(resource, annotationBody, provenance).toString();
  }

  public String subResourceExistsWithAnnotation(String id, String parentId, String type, String value) {
    final AlexandriaResource resource = subResourceExists(id, parentId);
    return hasConfirmedAnnotation(resource, anAnnotation(type, value)).toString();
  }

  private TentativeAlexandriaProvenance aProvenance(String who, Instant when) {
    return new TentativeAlexandriaProvenance(who, when, "why");
  }

}
