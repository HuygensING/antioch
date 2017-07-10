package nl.knaw.huygens.alexandria.annotation;

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
    List<String> tags = annotatedReferences.computeIfAbsent(id, k -> Lists.newArrayList());

    tags.add(tag);
  }

  public String addAnnotation(String id, String tag) {
    if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(tag)) {
      return "400 Bad Request";
    }

    List<String> tags = annotatedReferences.computeIfAbsent(id, k -> Lists.newArrayList());

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
