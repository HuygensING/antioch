package nl.knaw.huygens.alexandria.client;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

public class OptimisticAlexandriaClientTest extends AlexandriaTestWithTestServer {

  private static final String EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX = ".*\\.";
  private static OptimisticAlexandriaClient client;

  @BeforeClass
  public static void startClient() {
    client = new OptimisticAlexandriaClient("http://localhost:2016/");
  }

  @AfterClass
  public static void stopClient() {
    client.close();
  }

  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testAbout() {
    AboutEntity about = client.getAbout();
    assertThat(about.getVersion()).isNotEmpty();
  }

  @Test
  public void testOptimisticAlexandriaClientHasDelegatedUnwrappedMethodForEachRelevantMethodInAlexandriaClient() {
    Class<AlexandriaClient> a = AlexandriaClient.class;

    String stubs = Arrays.stream(a.getMethods())//
        .filter(this::returnsRestResult)//
        .filter(this::hasNoDelegatedMethodInOptimisticAlexandriaClient)//
        .map(this::toDelegatedMethodStub)//
        .collect(joining("\n"));
    Log.info("Methods to add to OptimisticAlexandriaClient:\n{}", stubs);
    assertThat(stubs).isEmpty();
  }

  @Test
  public void testNLA368() {
    // Request all annotations.
    // Show results per page,
    // sorted by date of last modification.
    // This should be an easy way for editors to check which annotations have been added/modified.
    UUID resource1UUID = createResource("ref1");
    UUID subResource1UUID = createSubResource(resource1UUID, "ref1.1");
    UUID resource2UUID = createResource("ref2");

    UUID annotationUUID1 = annotateResource(resource1UUID, "type1", "value1");
    UUID annotationUUID2 = annotateResource(resource2UUID, "type2", "value2");
    UUID annotationUUID3 = annotateResource(subResource1UUID, "type3", "value3");

    AlexandriaQuery query = new AlexandriaQuery()//
        .setFind("annotation")//
        .setReturns("id,who,type,value")//
        .setSort("-when")//
    ;
    UUID searchUUID = client.addSearch(query);
    SearchResultPage searchResultPage = client.getSearchResultPage(searchUUID);
    Log.info("searchResultPage={}", searchResultPage);
    List<Map<String, Object>> records = searchResultPage.getRecords();
    assertThat(records).hasSize(3);
    assertThat(records.get(0).get("id")).isEqualTo(annotationUUID3.toString());
    assertThat(records.get(1).get("id")).isEqualTo(annotationUUID2.toString());
    assertThat(records.get(2).get("id")).isEqualTo(annotationUUID1.toString());
    client.deprecateAnnotation(annotationUUID1);
    client.deprecateAnnotation(annotationUUID2);
    client.deprecateAnnotation(annotationUUID3);
  }

  @Test
  public void testNLA368a() {
    UUID resource1UUID = createResource("ref1");
    UUID subResource1UUID = createSubResource(resource1UUID, "ref1.1");
    UUID resource2UUID = createResource("ref2");

    UUID annotationUUID1 = annotateResource(resource1UUID, "type1", "value1");
    UUID annotationUUID2 = annotateResource(resource2UUID, "type2", "value2");
    UUID annotationUUID3 = annotateResource(subResource1UUID, "type3", "value3");
    updateAnnotation(annotationUUID2, "type2", "value4");

    AlexandriaQuery query = new AlexandriaQuery()//
        .setFind("annotation")//
        .setReturns("id,who,type,value")//
        .setSort("-when")//
    ;
    UUID searchUUID = client.addSearch(query);
    SearchResultPage searchResultPage = client.getSearchResultPage(searchUUID);
    Log.info("searchResultPage={}", searchResultPage);
    List<Map<String, Object>> records = searchResultPage.getRecords();
    assertThat(records).hasSize(3);
    assertThat(records.get(0).get("id")).isEqualTo(annotationUUID2.toString());
    assertThat(records.get(1).get("id")).isEqualTo(annotationUUID3.toString());
    assertThat(records.get(2).get("id")).isEqualTo(annotationUUID1.toString());
    assertThat(records.get(0).get("value")).isEqualTo("value4");
    client.deprecateAnnotation(annotationUUID1);
    client.deprecateAnnotation(annotationUUID2);
    client.deprecateAnnotation(annotationUUID3);
  }

  /// end tests

  protected UUID createResource(String resourceRef) {
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    UUID resourceUuid = UUID.randomUUID();
    client.setResource(resourceUuid, resource);
    return resourceUuid;
  }

  protected UUID createSubResource(UUID resourceUuid, String ref) {
    SubResourcePrototype subresource = new SubResourcePrototype().setSub(ref);
    return client.addSubResource(resourceUuid, subresource);
  }

  protected UUID annotateResource(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    return client.annotateResource(resourceUuid, annotationPrototype);
  }

  protected void updateAnnotation(UUID annotationUUID, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    client.updateAnnotation(annotationUUID, annotationPrototype);
  }

  boolean returnsRestResult(Method method) {
    return method.getReturnType().equals(RestResult.class);
  }

  boolean hasNoDelegatedMethodInOptimisticAlexandriaClient(Method method) {
    Class<OptimisticAlexandriaClient> o = OptimisticAlexandriaClient.class;
    try {
      Method oMethod = o.getMethod(method.getName(), method.getParameterTypes());
      Type type = actualReturnType(method);
      boolean equals = type.equals(Void.class)//
          ? oMethod.getReturnType().equals(Void.TYPE)//
          : oMethod.getReturnType().equals(type);
      return !equals;
    } catch (Exception e) {
      return true;
    }
  }

  private Type actualReturnType(Method method) {
    Type genericReturnType = method.getGenericReturnType();
    return ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
  }

  String toDelegatedMethodStub(Method method) {
    String returnType = actualReturnType(method).getTypeName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "").replace("Void", "void");
    String methodName = method.getName();
    String qualifiedParameters = Arrays.stream(method.getParameters())//
        .map(this::toQualifiedParameter)//
        .collect(joining(", "));
    String returnStatement = "void".equals(returnType) ? "" : "return ";
    String parameters = Arrays.stream(method.getParameters())//
        .map(this::parameterName)//
        .collect(joining(", "));

    return MessageFormat.format(//
        "public {0} {1}({2}) '{' {3}unwrap(delegate.{4}({5}));'}'", //
        returnType, //
        methodName, //
        qualifiedParameters, //
        returnStatement, //
        methodName, //
        parameters//
    );
  }

  String toQualifiedParameter(Parameter parameter) {
    return typeString(parameter) + " " + parameterName(parameter);
  }

  private String typeString(Parameter parameter) {
    return parameter.getType().getName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "");
  }

  String parameterName(Parameter parameter) {
    return typeString(parameter).toLowerCase();
  }

}
