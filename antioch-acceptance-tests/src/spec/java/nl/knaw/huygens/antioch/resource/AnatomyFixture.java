package nl.knaw.huygens.antioch.resource;

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

import static java.util.UUID.fromString;

import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends ResourcesBase {

  public String hasAnnotation(String id) {
    final UUID resId = fromString(id);
    final UUID annoId = service().annotate(theResource(resId), anAnnotationBody(resId), aProvenance()).getId();
    service().confirmAnnotation(annoId);
    return annoId.toString();
  }

  private AntiochAnnotationBody anAnnotationBody(UUID resId) {
    return service().createAnnotationBody(resId, aType(), aValue(), aProvenance());
  }

  private String aType() {
    return "type";
  }

  private String aValue() {
    return "value";
  }

}
