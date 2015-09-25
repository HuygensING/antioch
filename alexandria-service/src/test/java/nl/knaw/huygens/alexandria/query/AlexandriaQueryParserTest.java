package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

public class AlexandriaQueryParserTest {
  private AlexandriaQueryParser alexandriaQueryParser = new AlexandriaQueryParser(new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

  @Test
  public void testUnknownFindValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("foobar");
    try {
      alexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
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
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("huey");
      assertThat(e.getMessage()).contains("dewey");
      assertThat(e.getMessage()).contains("louie");
      assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testUnknownReturnValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFields("id,huey,dewey,louie");
    try {
      alexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("huey");
      assertThat(e.getMessage()).contains("dewey");
      assertThat(e.getMessage()).contains("louie");
      assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testReturnFields() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setFields("id, resource.id, subresource.id");
    ParsedAlexandriaQuery paq = alexandriaQueryParser.parse(aQuery);
    assertThat(paq.getReturnFields()).containsExactly("id", "resource.id", "subresource.id");
  }

  @Test
  public void testGenerateSortTokenFromString1() {
    SortToken st1 = AlexandriaQueryParser.sortToken("id");
    assertThat(st1.isAscending()).isTrue();
    assertThat(st1.getField()).isEqualTo(QueryField.id);
  }

  @Test
  public void testGenerateSortTokenFromString2() {
    SortToken st1 = AlexandriaQueryParser.sortToken("+when");
    assertThat(st1.isAscending()).isTrue();
    assertThat(st1.getField()).isEqualTo(QueryField.when);
  }

  @Test
  public void testGenerateSortTokenFromString3() {
    SortToken st1 = AlexandriaQueryParser.sortToken("-type");
    assertThat(st1.isAscending()).isFalse();
    assertThat(st1.getField()).isEqualTo(QueryField.type);
  }

  @Test
  public void testWhereTokenization() {
    String where = "type:eq(\"Tag\")"//
        + " who:eq(\"nederlab\")"//
        + " state:eq(\"CONFIRMED\")"//
        + " resource.id:inSet(\"11111-111-111-11-111\",\"11111-111-111-11-112\")";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    Log.info("errors:{}", alexandriaQueryParser.parseErrors);
    assertThat(tokens).hasSize(4);

    WhereToken typeToken = tokens.get(0);
    assertThat(typeToken.getProperty()).isEqualTo(QueryField.type);
    assertThat(typeToken.getFunction()).isEqualTo(QueryFunction.eq);
    assertThat(typeToken.getParameters()).containsExactly("Tag");

    WhereToken whoToken = tokens.get(1);
    assertThat(whoToken.getProperty()).isEqualTo(QueryField.who);
    assertThat(whoToken.getFunction()).isEqualTo(QueryFunction.eq);
    assertThat(whoToken.getParameters()).containsExactly("nederlab");

    WhereToken stateToken = tokens.get(2);
    assertThat(stateToken.getProperty()).isEqualTo(QueryField.state);
    assertThat(stateToken.getFunction()).isEqualTo(QueryFunction.eq);
    assertThat(stateToken.getParameters()).containsExactly("CONFIRMED");

    WhereToken resourceToken = tokens.get(3);
    assertThat(resourceToken.getProperty()).isEqualTo(QueryField.resource_id);
    assertThat(resourceToken.getFunction()).isEqualTo(QueryFunction.inSet);
    assertThat(resourceToken.getParameters()).containsExactly("11111-111-111-11-111", "11111-111-111-11-112");
  }

  @Test
  public void testTokenizingWhereClauseWithMissingQuoteAddsParseError() {
    String where = "type:eq(\"Tag)";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    List<String> parseErrors = alexandriaQueryParser.parseErrors;
    Log.info("errors:{}", parseErrors);
    assertThat(parseErrors).isNotEmpty();
    assertThat(tokens).isEmpty();
  }

  @Test
  public void testTokenizingWhereClauseWithIllegalFunctionAddsParseError() {
    String where = "type:not(1)";
    List<WhereToken> tokens = alexandriaQueryParser.tokenize(where);
    List<String> parseErrors = alexandriaQueryParser.parseErrors;
    Log.info("errors:{}", parseErrors);
    assertThat(parseErrors).isNotEmpty();
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

    assertThat(predicate.test(passingAnnotationVF)).isTrue();
    assertThat(predicate.test(failingAnnotationVF)).isFalse();
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

    assertThat(predicate.test(passingAnnotationVF)).isTrue();
    assertThat(predicate.test(passingAnnotationVF2)).isTrue();
    assertThat(predicate.test(failingAnnotationVF)).isFalse();
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
    aQuery.setFields("when,value,resource.id,subresource.id");
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
    Log.info("predicate={}", predicate);
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
    Log.info("alwaysTrue called {} times", times);
    return true;
  };
  AtomicInteger alwaysFalseCalled = new AtomicInteger(0);
  Predicate<Object> alwaysFalse = o -> {
    int times = alwaysFalseCalled.incrementAndGet();
    Log.info("alwaysFalse called {} times", times);
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

    assertThat(combination.test("whtvr")).isFalse();
    assertThat(alwaysTrueCalled.get()).isEqualTo(3);
    assertThat(alwaysFalseCalled.get()).isEqualTo(1);

    // alwaysTrueCalled = new AtomicInteger(0);
    // alwaysFalseCalled = new AtomicInteger(0);
    // Predicate<Object> parallel = list.parallelStream()//
    // .reduce(alwaysTrue, (p, np) -> p = p.and(np));
    //
    // assertThat(parallel.test("whtvr")).isFalse();
    // assertThat(alwaysTrueCalled.get()).isEqualTo(5);
    // assertThat(alwaysFalseCalled.get()).isEqualTo(1);
  }
}
