package nl.knaw.huygens.alexandria.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParser.SortToken;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

public class AlexandriaQueryParserTest {
  @Test
  public void testUnknownFindValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("foobar");
    try {
      AlexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("foobar");
    }
  }

  @Test
  public void testUnknownSortValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setSort("id,huey,duey,luey");
    try {
      AlexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("huey");
      assertThat(e.getMessage()).contains("duey");
      assertThat(e.getMessage()).contains("luey");
      assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testUnknownReturnValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFields("id,huey,duey,luey");
    try {
      AlexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("huey");
      assertThat(e.getMessage()).contains("duey");
      assertThat(e.getMessage()).contains("luey");
      assertThat(e.getMessage()).doesNotContain("unknown field: id");
    }
  }

  @Test
  public void testReturnFields() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setFields("id, resource.id, subresource.id");
    ParsedAlexandriaQuery paq = AlexandriaQueryParser.parse(aQuery);
    assertThat(paq.getReturnFields()).containsExactly("id", "resource.id", "subresource.id");
  }

  @Test
  public void testUserStory4a() {
    String userId = "USERID";
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setWhere("type.eq(\"Tag\"),who.eq(\"" + userId + "\"),state.eq(\"CONFIRMED\")");
    aQuery.setSort("when");
    aQuery.setFields("when,value,resource.id,subresource.id");
    ParsedAlexandriaQuery paq = AlexandriaQueryParser.parse(aQuery);

    AnnotationVF passAnnotation = mock(AnnotationVF.class);
    AnnotationBodyVF passBody = mock(AnnotationBodyVF.class);
    when(passBody.getType()).thenReturn("Tag");
    // .put("resource.id", AnnotationVF::getResourceId)//
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

    // find: test VFClass
    assertThat(paq.getVFClass()).isEqualTo(AnnotationVF.class);

    // test predicate
    Predicate<Traverser<AnnotationVF>> predicate = paq.getPredicate();
    // assertThat(predicate).isNotNull();
    //
    // Traverser<AnnotationVF> pass = mock(Traverser.class);
    // when(pass.get()).thenReturn(passAnnotation);
    // assertThat(predicate.test(pass)).isTrue();
    //
    // Traverser<AnnotationVF> fail = mock(Traverser.class);
    // when(fail.get()).thenReturn(failAnnotation);
    // assertThat(predicate.test(fail)).isFalse();

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

  @Test
  public void testGenerateSortTokenFromString1() {
    SortToken st1 = AlexandriaQueryParser.sortToken("field1");
    assertThat(st1.isAscending()).isTrue();
    assertThat(st1.getField()).isEqualTo("field1");
  }

  @Test
  public void testGenerateSortTokenFromString2() {
    SortToken st1 = AlexandriaQueryParser.sortToken("+field2");
    assertThat(st1.isAscending()).isTrue();
    assertThat(st1.getField()).isEqualTo("field2");
  }

  @Test
  public void testGenerateSortTokenFromString3() {
    SortToken st1 = AlexandriaQueryParser.sortToken("-field3");
    assertThat(st1.isAscending()).isFalse();
    assertThat(st1.getField()).isEqualTo("field3");
  }

}
