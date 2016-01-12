package nl.knaw.huygens.alexandria.endpoint.resource;

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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MatchesPathIdValidatorTest {

  @Test
  @Parameters({ //
      "fe944b98-18b1-11e5-9d51-1f35c61c7a23, fe944b98-18b1-11e5-9d51-1f35c61c7a23, true", // same UUIDs
      "fe944b98-18b1-11e5-9d51-1f35c61c7a23, 27947b26-18b2-11e5-be1c-c7fb9bd13f9b, false" // different UUIDs
  })
  public void testValidationOfUUIDinURIShouldMatchUUIDinPrototype(String uriId, String protoId, boolean valid) {
    final PathSegment mockedPathSegment = mock(PathSegment.class);
    when(mockedPathSegment.getPath()).thenReturn(protoId);

    final UriInfo mockedUriInfo = mock(UriInfo.class);
    when(mockedUriInfo.getPathSegments()).thenReturn(Lists.newArrayList(mockedPathSegment));

    final ResourcePrototype mockedPrototype = mock(ResourcePrototype.class);
    when(mockedPrototype.getId()).thenReturn(new UUIDParam(uriId));

    assertThat(new Validator(mockedUriInfo).isValid(mockedPrototype, null), is(valid));

    verify(mockedUriInfo, atLeastOnce()).getPathSegments();
    verify(mockedPrototype).getId();
  }

}
