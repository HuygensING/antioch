package nl.knaw.huygens.alexandria.endpoint.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.Validator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MatchesPathIdValidatorTest {
  private UriInfo mockedUriInfo;
  private PathSegment mockedPathSegment;
  private ResourcePrototype mockedPrototype;
  private Validator validator;

  @Before
  public void setup() {
    mockedUriInfo = mock(UriInfo.class);
    mockedPathSegment = mock(PathSegment.class);
    mockedPrototype = mock(ResourcePrototype.class);
    validator = new Validator(mockedUriInfo);
  }

  @Test
  public void differentIdInURIAndPrototypeShouldBeInvalid() throws Exception {
    when(mockedPrototype.getId()).thenReturn(new UUIDParam(someUUID()));
    when(mockedUriInfo.getPathSegments()).thenReturn(Lists.newArrayList(mockedPathSegment));
    when(mockedPathSegment.getPath()).thenReturn(anotherUUID());

    Assert.assertFalse(validator.isValid(mockedPrototype, null));
  }

  @Test
  public void sameIdInURIAndPrototypeShouldBeValid() throws Exception {
    when(mockedPrototype.getId()).thenReturn(new UUIDParam(someUUID()));
    when(mockedUriInfo.getPathSegments()).thenReturn(Lists.newArrayList(mockedPathSegment));
    when(mockedPathSegment.getPath()).thenReturn(someUUID());

    Assert.assertTrue(validator.isValid(mockedPrototype, null));
  }

  private String someUUID() {
    return "fe944b98-18b1-11e5-9d51-1f35c61c7a23";
  }

  private String anotherUUID() {
    return "27947b26-18b2-11e5-be1c-c7fb9bd13f9b";
  }
}