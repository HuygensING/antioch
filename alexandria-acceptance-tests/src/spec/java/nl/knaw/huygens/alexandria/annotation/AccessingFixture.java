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
import static org.concordion.api.MultiValueResult.multiValueResult;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.endpoint.search.SearchEndpoint;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;

@RunWith(ConcordionRunner.class)
public class AccessingFixture extends AnnotationsBase {

  @BeforeClass
  public static void registerSearchEndpoint() {
    register(SearchEndpoint.class);
  }

  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();

  public void resourceExistsWithAnnotationInState(String resIdStr, String annoState, String annoValue) {
    Log.trace("resourceExistsWithAnnotationInState: resIdStr=[{}], annoState=[{}]", resIdStr, annoState);
    final UUID resId = fromString(resIdStr);
    final UUID annoBodyId = UUID.randomUUID();
    final AlexandriaAnnotationBody annoBody = service().createAnnotationBody(annoBodyId, "t", annoValue, aProvenance());
    final AlexandriaAnnotation annotation = service().annotate(theResource(resId), annoBody, aProvenance());
    final UUID annoId = annotation.getId();
    Log.trace("annotation.id: [{}]", annoId);

    switch (annoState) {
    case "tentative":
      // no-op intentional: newly created annotations are tentative by default
      Assert.assertEquals(AlexandriaState.TENTATIVE, annotation.getState());
      break;
    case "confirmed":
      service().confirmAnnotation(annoId);
      break;
    case "deleted-after-confirmation":
      service().confirmAnnotation(annoId);
      service().deleteAnnotation(annotation);
      break;
    case "deleted-before-confirmation":
      service().deleteAnnotation(annotation);
      break;
    case "deprecated":
      service().confirmAnnotation(annoId);
      final AlexandriaAnnotation revisedAnno = service().annotate(theResource(resId), annoBody, aProvenance());
      service().deprecateAnnotation(annoId, revisedAnno);
      break;
    default:
      Assert.assertTrue("Never reached", false);
      break;
    }
  }

  public void noSuchAnnotation(String id) {
    when(service().readAnnotation(asUUID(id))).thenThrow(new NotFoundException());
  }

  public void setUpAnnotation(String id, String tag) {
    List<String> tags = annotatedReferences.get(id);

    if (tags == null) {
      tags = Lists.newArrayList();
      annotatedReferences.put(id, tags);
    }

    tags.add(tag);
  }

  public String addAnnotation(String id, String tag) {
    if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(tag)) {
      return "400 Bad Request";
    }

    List<String> tags = annotatedReferences.get(id);
    if (tags == null) {
      tags = Lists.newArrayList();
      annotatedReferences.put(id, tags);
    }

    if (tags.contains(tag)) {
      return "409 Conflict";
    }

    tags.add(tag);
    return "201 Created";
  }

  public MultiValueResult getAnnotation(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return multiValueResult().with("status", "400 Bad Request");
    }

    if (annotatedReferences.containsKey(id)) {
      return multiValueResult().with("status", "200 Ok").with("value", annotatedReferences.get(id));
    }

    return multiValueResult().with("status", "404 Not Found");
  }

  private UUID asUUID(String s) {
    return fromString(s);
  }

}
