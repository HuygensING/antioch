package nl.knaw.huygens.alexandria.annotation;

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

import static java.util.UUID.fromString;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends AnnotationsBase {

  @Override
  public void resourceExists(String id) {
    service().createOrUpdateResource(fromString(id), aRef(), aProvenance(), AlexandriaState.CONFIRMED);
  }

  public String hasAnnotation(String id) {
    final UUID uuid = fromString(id);
    return annotate(theResource(uuid), anAnnotationBody(uuid), aProvenance());
  }

  private AlexandriaAnnotationBody anAnnotationBody(UUID resId) {
    return service().createAnnotationBody(resId, aType(), aValue(), aProvenance());
  }

  private String aType() {
    return "type";
  }

  private String aValue() {
    return "value";
  }

  @SuppressWarnings("unused")
  private TentativeAlexandriaProvenance aProvenance(String who, Instant when) {
    return new TentativeAlexandriaProvenance(who, when, "why");
  }

}
