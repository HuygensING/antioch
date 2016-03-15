package nl.knaw.huygens.alexandria.annotation;

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
