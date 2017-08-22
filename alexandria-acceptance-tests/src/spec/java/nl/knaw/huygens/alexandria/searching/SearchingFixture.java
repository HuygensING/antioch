package nl.knaw.huygens.alexandria.searching;

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

import static java.util.UUID.randomUUID;

import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.search.SearchEndpoint;

@RunWith(ConcordionRunner.class)
public class SearchingFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoints() {
    register(SearchEndpoint.class);
  }

  public void setupPagingStorage(String num) {
    clearStorage();

    for (int i = 0; i < Integer.valueOf(num); i++) {
      generateAnnotatedResource(randomUUID());
    }
  }

  private void generateAnnotatedResource(UUID uuid) {
    resourceExists(uuid.toString());
    hasConfirmedAnnotation(theResource(uuid), anAnnotation());
  }

}
