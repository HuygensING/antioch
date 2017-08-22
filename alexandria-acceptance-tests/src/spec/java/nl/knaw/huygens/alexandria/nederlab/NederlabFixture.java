package nl.knaw.huygens.alexandria.nederlab;

/*
 * #%L
 * alexandria-acceptance-tests
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
