package nl.knaw.huygens.alexandria.endpoint;

/*
 * #%L
 * alexandria-main
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

import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;

import com.google.inject.Module;

import nl.knaw.huygens.alexandria.EndpointTest;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class MockedServiceEndpointTest extends EndpointTest {
  protected static final AlexandriaService SERVICE_MOCK = mock(AlexandriaService.class);

  @BeforeClass
  public static void setup() {
    Module baseModule = new TestModule(SERVICE_MOCK);
    setupWithModule(baseModule);
  }

}
