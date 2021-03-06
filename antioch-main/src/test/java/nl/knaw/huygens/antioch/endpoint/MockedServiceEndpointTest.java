package nl.knaw.huygens.antioch.endpoint;

/*
 * #%L
 * antioch-main
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

import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;

import com.google.inject.Module;

import nl.knaw.huygens.antioch.EndpointTest;
import nl.knaw.huygens.antioch.service.AntiochService;

public class MockedServiceEndpointTest extends EndpointTest {
  protected static final AntiochService SERVICE_MOCK = mock(AntiochService.class);

  @BeforeClass
  public static void setup() {
    Module baseModule = new TestModule(SERVICE_MOCK);
    setupWithModule(baseModule);
  }

}
