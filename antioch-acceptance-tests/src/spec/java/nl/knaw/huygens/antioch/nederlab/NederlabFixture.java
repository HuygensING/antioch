package nl.knaw.huygens.antioch.nederlab;

/*
 * #%L
 * antioch-acceptance-tests
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

import nl.knaw.huygens.antioch.concordion.AntiochAcceptanceTest;
import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.antioch.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.antioch.endpoint.search.SearchEndpoint;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;

@RunWith(ConcordionRunner.class)
public class NederlabFixture extends AntiochAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(SearchEndpoint.class);
  }

  public AntiochResource subResourceExists(String id, String parentId) {
    UUID resourceId = fromString(id);
    UUID parentUUID = fromString(parentId);
    return service().createSubResource(resourceId, parentUUID, aSub(), aProvenance());
  }

  public String resourceHasAnnotation(String id) {
    return resourceHasAnnotation(id, "type", "value");
  }

  public String resourceHasAnnotation(String resId, String type, String value) {
    final AntiochResource resource = theResource(fromString(resId));
    final AntiochAnnotationBody annotationBody = anAnnotation(type, value);
    return hasConfirmedAnnotation(resource, annotationBody).toString();
  }

  public String resourceExistsWithTagForUserAtInstant(String resId, String value, String who, String when) {
    resourceExists(resId);

    final AntiochResource resource = theResource(fromString(resId));

    final Instant whenAsInstant = ISO_DATE_TIME.parse(when, Instant::from);
    final TentativeAntiochProvenance provenance = aProvenance(who, whenAsInstant);
    final AntiochAnnotationBody annotationBody = anAnnotation("Tag", value, provenance);

    return hasConfirmedAnnotation(resource, annotationBody, provenance).toString();
  }

  public String subResourceExistsWithAnnotation(String id, String parentId, String type, String value) {
    final AntiochResource resource = subResourceExists(id, parentId);
    return hasConfirmedAnnotation(resource, anAnnotation(type, value)).toString();
  }

  private TentativeAntiochProvenance aProvenance(String who, Instant when) {
    return new TentativeAntiochProvenance(who, when, "why");
  }

}
