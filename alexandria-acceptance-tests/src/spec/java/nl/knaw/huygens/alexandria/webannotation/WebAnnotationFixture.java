package nl.knaw.huygens.alexandria.webannotation;

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

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.iiif.IIIFAnnotationListEndpoint;
import nl.knaw.huygens.alexandria.endpoint.iiif.IIIFEndpoint;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.jersey.exceptionmappers.WebApplicationExceptionMapper;

@RunWith(ConcordionRunner.class)
// @ExpectedToFail
public class WebAnnotationFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(WebAnnotationsEndpoint.class);
    register(WebApplicationExceptionMapper.class);
    register(IIIFAnnotationListEndpoint.class);
    register(IIIFEndpoint.class);
  }

}
