package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.alexandria.api.model.ErrorEntity;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.search.QueryFunction;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlexandriaQueryParserTest extends AlexandriaTest {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaQueryParserTest.class);
  private AlexandriaQueryParser alexandriaQueryParser = new AlexandriaQueryParser(new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

  @Test
  public void testUnknownFindValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("foobar");
    try {
      alexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      LOG.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("foobar");
    }
  }

  @Test
  public void testUnknownSortValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setSort("id,huey,dewey,louie");
    try {
      alexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      LOG.info("error message: {}", e.getMessage());
      softly.assertThat(e.getMessage()).contains("huey");
      softly.assertThat(e.getMessage()).contains("dewey");
      softly.assertThat(e.getMessage()).contains("louie");
      softly.assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testUnknownReturnValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setReturns("id,huey,dewey,louie");
    try {
      alexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      LOG.info("error message: {}", e.getMessage());
      softly.assertThat(e.getMessage()).contains("huey");
      softly.assertThat(e.getMessage()).contains("dewey");
      softly.assertThat(e.getMessage()).contains("louie");
      softly.assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testReturnFields() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setReturns("id, resource.id, subresource.id");
    ParsedAlexandriaQuery paq = alexandriaQueryParser.parse(aQuery);
    assertThat(paq.getReturnFields()).containsExactly("id", "resource.id", "subresource.id");
    assertThat(paq.getFieldsToGroup()).isEmpty();
  }

  @Test
  public void testReturnFieldsWithList() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setReturns("list(id), resource.id, resource.url");
    ParsedAlexandriaQuery paq = alexandriaQueryParser.parse(aQuery);
    assertThat(paq.getReturnFields()).containsExactly("id", "resource.id", "resource.url");
    assertThat(paq.getFieldsToGroup()).containsExactly("id");

    Map<String, Object> map = ImmutableMap.of("id", "Id", "resource.id", "Resource.id", "resource.url", "Resource.URL");
    assertThat(paq.concatenateGroupByFieldsValues(map)).isEqualTo("Resource.idResource.URL");

    Map<String, Object> map1 = new HashMap<>();
    map1.put("id", "Id1");
    map1.put("resource.id", "Resource.id");
    map1.put("resource.url", "Resource.URL");
    Map<String, Object> map2 = ImmutableMap.of("id", "Id2", "resource.id", "Resource.id", "resource.url", "Resource.URL");
    Map<String, Object> map3 = ImmutableMap.of("id", "Id3", "resource.id", "Resource.id", "resource.url", "Resource.URL");
    List<Map<String, Object>> mapList = ImmutableList.of(map1, map2, map3);
    Map<String, Object> expected = ImmutableMap.of(//
        "_list", ImmutableList.of(ImmutableMap.of("id", "Id1"), ImmutableMap.of("id", "Id2"), ImmutableMap.of("id", "Id3")), //
        "resource.id", "Resource.id", //
        "resource.url", "Resource.URL"//
    );
    assertThat(paq.collectListFieldValues(mapList)).containsAllEntriesOf(expected);
  }

  @Test
  public void testGenerateSortTokenFromString1() {
    SortToken st1 = AlexandriaQueryParser.sortToken("id");
    softly.assertThat(st1.isAscending()).isTrue();
    softly.assertThat(st1.getField()).isEqualTo(QueryField.id);
  }

  @Test
  public void testGenerateSortTokenFromString2() {
    SortToken st1 = AlexandriaQueryParser.sortToken("+when");
    softly.assertThat(st1.isAscending()).isTrue();
    softly.assertThat(st1.getField()).isEqualTo(QueryField.when);
  }

  @Test
  public void testGenerateSortTokenFromString3() {
    SortToken st1 = AlexandriaQueryParser.sortToken("-type");
    softly.assertThat(st1.isAscending()).isFalse();
    softly.assertThat(st1.getField()).isEqualTo(QueryField.type);
  }

  @Test
  public void testWhereTokenization() {
    String where = "type:eq(\"Tag\")"//
        + " who:eq(\"nederlab\")"//
        + " state:eq(\"CONFIRMED\")"//
        + " resource.id:inSet(\"11111-111-111-11-111\",\"11111-111-111-11-112\")";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    LOG.info("errors:{}", alexandriaQueryParser.parseErrors);
    assertThat(tokens).hasSize(4);

    WhereToken typeToken = tokens.get(0);
    softly.assertThat(typeToken.getProperty()).isEqualTo(QueryField.type);
    softly.assertThat(typeToken.getFunction()).isEqualTo(QueryFunction.eq);
    softly.assertThat(typeToken.getParameters()).containsExactly("Tag");

    WhereToken whoToken = tokens.get(1);
    softly.assertThat(whoToken.getProperty()).isEqualTo(QueryField.who);
    softly.assertThat(whoToken.getFunction()).isEqualTo(QueryFunction.eq);
    softly.assertThat(whoToken.getParameters()).containsExactly("nederlab");

    WhereToken stateToken = tokens.get(2);
    softly.assertThat(stateToken.getProperty()).isEqualTo(QueryField.state);
    softly.assertThat(stateToken.getFunction()).isEqualTo(QueryFunction.eq);
    softly.assertThat(stateToken.getParameters()).containsExactly("CONFIRMED");

    WhereToken resourceToken = tokens.get(3);
    softly.assertThat(resourceToken.getProperty()).isEqualTo(QueryField.resource_id);
    softly.assertThat(resourceToken.getFunction()).isEqualTo(QueryFunction.inSet);
    softly.assertThat(resourceToken.getParameters()).containsExactly("11111-111-111-11-111", "11111-111-111-11-112");
  }

  @Test
  public void testTokenizingWhereClauseWithMissingQuoteAddsParseError() {
    String where = "type:eq(\"Tag)";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    List<String> parseErrors = alexandriaQueryParser.parseErrors;
    LOG.info("errors:{}", parseErrors);
    softly.assertThat(parseErrors).isNotEmpty();
    softly.assertThat(tokens).isEmpty();
  }

  @Test
  public void testTokenizingWhereClauseWithIllegalFunctionAddsParseError() {
    String where = "type:not(1)";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    List<String> parseErrors = alexandriaQueryParser.parseErrors;
    LOG.info("errors:{}", parseErrors);
    softly.assertThat(parseErrors).isNotEmpty();
    assertThat(tokens).isEmpty();
  }

  @Test
  public void testPredicateForWhoEq() {
    WhereToken whereToken = new WhereToken(QueryField.who, QueryFunction.eq, ImmutableList.of("Gremlin"));
    AnnotationVF passingAnnotationVF = mock(AnnotationVF.class);
    when(passingAnnotationVF.getProvenanceWho()).thenReturn("Gremlin");

    AnnotationVF failingAnnotationVF = mock(AnnotationVF.class);
    when(failingAnnotationVF.getProvenanceWho()).thenReturn("SomeoneElse");

    Predicate<AnnotationVF> predicate = AlexandriaQueryParser.toPredicate(whereToken);

    assertThat(predicate.test(passingAnnotationVF)).isTrue();
    assertThat(predicate.test(failingAnnotationVF)).isFalse();
  }

  @Test
  public void testPredicateForTypeEq() {
    WhereToken whereToken = new WhereToken(QueryField.type, QueryFunction.eq, ImmutableList.of("Tag"));
    AnnotationVF passingAnnotationVF = mock(AnnotationVF.class);
    when(passingAnnotationVF.getType()).thenReturn("Tag");

    AnnotationVF failingAnnotationVF = mock(AnnotationVF.class);
    when(failingAnnotationVF.getType()).thenReturn("Whatever");

    Predicate<AnnotationVF> predicate = AlexandriaQueryParser.toPredicate(whereToken);

    assertThat(predicate.test(passingAnnotationVF)).isTrue();
    assertThat(predicate.test(failingAnnotationVF)).isFalse();
  }

  @Test
  public void testPredicateForStateEq() {
    WhereToken whereToken = new WhereToken(QueryField.state, QueryFunction.eq, ImmutableList.of("CONFIRMED"));
    AnnotationVF passingAnnotationVF = mock(AnnotationVF.class);
    when(passingAnnotationVF.getState()).thenReturn("CONFIRMED");

    AnnotationVF failingAnnotationVF = mock(AnnotationVF.class);
    when(failingAnnotationVF.getState()).thenReturn("TENTATIVE");

    Predicate<AnnotationVF> predicate = AlexandriaQueryParser.toPredicate(whereToken);

    softly.assertThat(predicate.test(passingAnnotationVF)).isTrue();
    softly.assertThat(predicate.test(failingAnnotationVF)).isFalse();
  }

  @Test
  public void testPredicateForValueMatch() {
    WhereToken whereToken = new WhereToken(QueryField.value, QueryFunction.match, ImmutableList.of("super.*"));
    AnnotationVF passingAnnotationVF = mock(AnnotationVF.class);
    when(passingAnnotationVF.getValue()).thenReturn("supergirl");
    AnnotationVF passingAnnotationVF2 = mock(AnnotationVF.class);
    when(passingAnnotationVF2.getValue()).thenReturn("superman");

    AnnotationVF failingAnnotationVF = mock(AnnotationVF.class);
    when(failingAnnotationVF.getValue()).thenReturn("batman");

    Predicate<AnnotationVF> predicate = AlexandriaQueryParser.toPredicate(whereToken);

    softly.assertThat(predicate.test(passingAnnotationVF)).isTrue();
    softly.assertThat(predicate.test(passingAnnotationVF2)).isTrue();
    softly.assertThat(predicate.test(failingAnnotationVF)).isFalse();
  }

  @Test //
  public void testPredicateForWhenInRange() {
    WhereToken whereToken = new WhereToken(QueryField.when, QueryFunction.inRange, ImmutableList.of("20150101", "20151231"));
    AnnotationVF passingAnnotationVF = mock(AnnotationVF.class);
    when(passingAnnotationVF.getProvenanceWhen()).thenReturn("20150615");

    AnnotationVF failingAnnotationVF = mock(AnnotationVF.class);
    when(failingAnnotationVF.getProvenanceWhen()).thenReturn("20160101");

    Predicate<AnnotationVF> predicate = AlexandriaQueryParser.toPredicate(whereToken);

    assertThat(predicate.test(passingAnnotationVF)).isTrue();
    assertThat(predicate.test(failingAnnotationVF)).isFalse();
  }

  @Test
  public void testUserStory4a() {
    String userId = "USERID";
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setWhere(//
        "type:eq(\"Tag\")"//
            + " who:eq(\"" + userId + "\")"//
            + " state:eq(\"CONFIRMED\")"//
    );
    aQuery.setSort("when");
    aQuery.setReturns("when,value,resource.id,subresource.id");
    ParsedAlexandriaQuery paq = alexandriaQueryParser.parse(aQuery);

    AnnotationVF passAnnotation = mock(AnnotationVF.class);
    AnnotationBodyVF passBody = mock(AnnotationBodyVF.class);
    when(passBody.getType()).thenReturn("Tag");
    // .put(QueryField.resource_id, AnnotationVF::getResourceId)//
    // .put("subresource.id", AnnotationVF::getSubResourceId)//

    when(passAnnotation.getUuid()).thenReturn("uuid");
    when(passAnnotation.getBody()).thenReturn(passBody);
    when(passAnnotation.getType()).thenReturn("Tag");
    when(passAnnotation.getValue()).thenReturn("Value");
    when(passAnnotation.getProvenanceWhen()).thenReturn("1");
    when(passAnnotation.getProvenanceWho()).thenReturn(userId);
    when(passAnnotation.getProvenanceWhy()).thenReturn("because");
    when(passAnnotation.getResourceId()).thenReturn("resourceId");
    when(passAnnotation.getSubResourceId()).thenReturn("subresourceId");
    when(passAnnotation.getState()).thenReturn("CONFIRMED");

    AnnotationVF failAnnotation = mock(AnnotationVF.class);
    when(failAnnotation.getBody()).thenReturn(passBody);
    when(failAnnotation.getProvenanceWhen()).thenReturn("2");
    when(failAnnotation.getProvenanceWho()).thenReturn(userId);
    when(failAnnotation.getState()).thenReturn("TENTATIVE");
    when(failAnnotation.getType()).thenReturn("Tag");

    // find: test VFClass
    assertThat(paq.getVFClass()).isEqualTo(AnnotationVF.class);

    // test predicate
    Predicate<AnnotationVF> predicate = paq.getPredicate();
    LOG.info("predicate={}", predicate);
    assertThat(predicate).isNotNull();
    assertThat(predicate.test(passAnnotation)).isTrue();
    assertThat(predicate.test(failAnnotation)).isFalse();

    // sort: test resultComparator
    Comparator<AnnotationVF> resultComparator = paq.getResultComparator();
    int compare1 = resultComparator.compare(passAnnotation, failAnnotation);
    assertThat(compare1).isEqualTo(-1);
    int compare2 = resultComparator.compare(failAnnotation, passAnnotation);
    assertThat(compare2).isEqualTo(1);
    int compare3 = resultComparator.compare(passAnnotation, passAnnotation);
    assertThat(compare3).isEqualTo(0);

    // return: test returnFields + resultMapper
    assertThat(paq.getReturnFields()).containsExactly("when", "value", "resource.id", "subresource.id");
    Function<AnnotationVF, Map<String, Object>> resultMapper = paq.getResultMapper();
    Map<String, Object> resultMap = resultMapper.apply(passAnnotation);
    assertThat(resultMap).contains( //
        MapEntry.entry("when", "1"), //
        MapEntry.entry("value", "Value"), //
        MapEntry.entry("resource.id", "resourceId"), //
        MapEntry.entry("subresource.id", "subresourceId") //
    );
  }

  AtomicInteger alwaysTrueCalled = new AtomicInteger(0);
  Predicate<Object> alwaysTrue = o -> {
    int times = alwaysTrueCalled.incrementAndGet();
    LOG.info("alwaysTrue called {} times", times);
    return true;
  };
  AtomicInteger alwaysFalseCalled = new AtomicInteger(0);
  Predicate<Object> alwaysFalse = o -> {
    int times = alwaysFalseCalled.incrementAndGet();
    LOG.info("alwaysFalse called {} times", times);
    return false;
  };

  @Test
  public void testPredicates() {
    List<Predicate<Object>> list = new ArrayList<>();
    list.add(alwaysTrue);
    list.add(alwaysTrue);
    list.add(alwaysFalse);
    list.add(alwaysFalse);
    list.add(alwaysFalse);
    list.add(alwaysFalse);
    list.add(alwaysTrue);
    list.add(alwaysTrue);
    list.add(alwaysTrue);

    Predicate<Object> combination = list.stream()//
        .reduce(alwaysTrue, (p, np) -> p = p.and(np));

    softly.assertThat(combination.test("whtvr")).isFalse();
    softly.assertThat(alwaysTrueCalled.get()).isEqualTo(3);
    softly.assertThat(alwaysFalseCalled.get()).isEqualTo(1);

    // alwaysTrueCalled = new AtomicInteger(0);
    // alwaysFalseCalled = new AtomicInteger(0);
    // Predicate<Object> parallel = list.parallelStream()//
    // .reduce(alwaysTrue, (p, np) -> p = p.and(np));
    //
    // assertThat(parallel.test("whtvr")).isFalse();
    // assertThat(alwaysTrueCalled.get()).isEqualTo(5);
    // assertThat(alwaysFalseCalled.get()).isEqualTo(1);
  }

  @Test
  public void testGetAnnotationIdFromDeprecatedAnnotationRemovesRevisionNumber() {
    AnnotationVF avf = mock(AnnotationVF.class);
    String randomUUID = UUID.randomUUID().toString();
    when(avf.getUuid()).thenReturn(randomUUID + ".0");

    String annotationId = AlexandriaQueryParser.getAnnotationId(avf);

    assertThat(annotationId).isEqualTo(randomUUID);
  }

  @Test
  public void testInvalidStateInStateEqThrowsBadRequestException() {
    WhereToken whereToken = new WhereToken(QueryField.state, QueryFunction.eq, ImmutableList.of("RUBBISH"));
    try {
      AlexandriaQueryParser.toPredicate(whereToken);
      fail("expected BadRequestException");
    } catch (BadRequestException e) {
      String responseMessage = ((ErrorEntity) e.getResponse().getEntity()).getMessage();
      assertThat(responseMessage).isEqualTo("RUBBISH is not a valid value for state");
    }
  }

  @Test
  public void testOneInvalidStatesInStateInSetThrowsBadRequestException() {
    WhereToken whereToken = new WhereToken(QueryField.state, QueryFunction.eq, ImmutableList.of("RUBBISH", "CONFIRMED", "TENTATIVE"));
    try {
      AlexandriaQueryParser.toPredicate(whereToken);
      fail("expected BadRequestException");
    } catch (BadRequestException e) {
      String responseMessage = ((ErrorEntity) e.getResponse().getEntity()).getMessage();
      assertThat(responseMessage).isEqualTo("RUBBISH is not a valid value for state");
    }
  }

  @Test
  public void testTwoInvalidStatesInStateInSetThrowsBadRequestException() {
    WhereToken whereToken = new WhereToken(QueryField.state, QueryFunction.eq, ImmutableList.of("RUBBISH", "GARBAGE", "CONFIRMED"));
    try {
      AlexandriaQueryParser.toPredicate(whereToken);
      fail("expected BadRequestException");
    } catch (BadRequestException e) {
      String responseMessage = ((ErrorEntity) e.getResponse().getEntity()).getMessage();
      assertThat(responseMessage).isEqualTo("RUBBISH, GARBAGE are not valid values for state");
    }
  }
}
