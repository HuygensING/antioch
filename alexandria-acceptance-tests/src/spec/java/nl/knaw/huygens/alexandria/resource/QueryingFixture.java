package nl.knaw.huygens.alexandria.resource;

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

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class QueryingFixture extends ResourcesBase {
  private AlexandriaResource resource;

  public void existingResource(String id) {
    UUID uuid = UUID.fromString(id);
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    resource = new AlexandriaResource(uuid, provenance);
    Optional<AlexandriaResource> optional = Optional.of(resource);
    when(service().readResource(uuid)).thenReturn(optional);
  }

  public void withReference(String reference) {
    resource.setCargo(reference);
  }

  public void withAnnotation(String id) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaAnnotationBody body = new AlexandriaAnnotationBody(UUID.fromString(id), "<type>", "<value>", provenance);
    resource.addAnnotation(new AlexandriaAnnotation(UUID.fromString(id), body, provenance));
  }

  public void noSuchResource(String id) {
    UUID uuid = UUID.fromString(id);
    when(service().readResource(uuid)).thenThrow(new NotFoundException());
  }
}
