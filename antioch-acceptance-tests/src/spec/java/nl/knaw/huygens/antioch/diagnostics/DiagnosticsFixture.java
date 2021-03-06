package nl.knaw.huygens.antioch.diagnostics;

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

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.concordion.AntiochAcceptanceTest;
import nl.knaw.huygens.antioch.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.antioch.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.antioch.jersey.exceptionmappers.NotFoundExceptionMapper;

@RunWith(ConcordionRunner.class)
public class DiagnosticsFixture extends AntiochAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    Log.trace("Registering endpoints");
    register(AboutEndpoint.class);
    register(ResourcesEndpoint.class);
    register(NotFoundExceptionMapper.class);
  }
}
