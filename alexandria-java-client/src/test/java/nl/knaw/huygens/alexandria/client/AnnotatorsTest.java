package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;

public class AnnotatorsTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testSetThenGetAnnotator() {
    String resourceRef = "test";
    UUID resourceUuid = createResource(resourceRef);
    String code = "abc";
    String description = "Annotator description";
    setResourceAnnotator(resourceUuid, code, description);

    RestResult<Annotator> getResult = client.getAnnotator(resourceUuid, code);
    assertRequestSucceeded(getResult);
    Annotator annotator2 = getResult.get();
    assertThat(annotator2.getCode()).isEqualTo(code);
    assertThat(annotator2.getDescription()).isEqualTo(description);
    assertThat(annotator2.getResourceURI()).hasToString("http://localhost:2016/resources/" + resourceUuid);
  }

  private void setResourceAnnotator(UUID resourceUuid, String code, String description) {
    Annotator annotator = new Annotator().setDescription(description);
    RestResult<Void> putResult = client.setAnnotator(resourceUuid, code, annotator);
    assertRequestSucceeded(putResult);
    // URI uri = putResult.get();
    // assertThat(uri).hasToString("http://localhost:2016/resources/" + resourceUuid + "/annotators/abc");
  }

  @Test
  public void testAnnotators() {
    String resourceRef = "test";
    UUID resourceUUID = createResource(resourceRef);
    AnnotatorList annotatorList = getAnnotatorList(resourceUUID);
    assertThat(annotatorList).isEmpty();

    UUID subresourceUUID = createSubResource(resourceUUID, "ref");
    String code = "abc";
    String description = "Annotator abc";
    setResourceAnnotator(resourceUUID, code, description);
    AnnotatorList annotatorList2 = getAnnotatorList(subresourceUUID);
    assertThat(annotatorList2).hasSize(1);
    Annotator annotator = annotatorList2.get(0);
    assertThat(annotator.getCode()).isEqualTo(code);
    assertThat(annotator.getDescription()).isEqualTo(description);
  }

  private AnnotatorList getAnnotatorList(UUID resourceUUID) {
    RestResult<AnnotatorList> result = client.getAnnotators(resourceUUID);
    assertRequestSucceeded(result);
    return result.get();
  }

}
