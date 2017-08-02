package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import static java.util.stream.Collectors.joining;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  public void testNLA369() throws InterruptedException {
    // A user requests a list of all their annotations.
    // Current situation: Results are shown grouped by resource and sorted by the date of
    // the first annotation added to that resource.
    // Desired situation: Results are shown grouped by resource, but sorted by date of
    // last modification (any modification) of annotations on that resource (changes,
    // additions and deletions). This also includes annotations on subresources.
    UUID resource1UUID = createResource("ref1");
    UUID subResource1UUID = createSubResource(resource1UUID, "ref1.1");
    UUID resource2UUID = createResource("ref2");

    UUID annotationUUID1 = annotateResource(resource1UUID, "type1", "value1");
    Thread.sleep(1000); // to increase creation time diff.
    UUID annotationUUID2 = annotateResource(resource2UUID, "type2", "value2");
    Thread.sleep(1000);
    UUID annotationUUID3 = annotateResource(subResource1UUID, "type3", "value3");

    AlexandriaQuery query = new AlexandriaQuery()//
        .setFind("annotation")//
        .setWhere("who:eq(\"testuser\")")//
        .setReturns("resource.id,list(id,who,type,value,when)")//
        .setSort("-when")//
        ;
    UUID searchUUID = client.addSearch(query);
    SearchResultPage searchResultPage = client.getSearchResultPage(searchUUID);
    Log.info("searchResultPage={}", searchResultPage);
    List<Map<String, Object>> records = searchResultPage.getRecords();
    assertThat(records).hasSize(2); // 2 (non-sub) resources

    Map<String, Object> record1 = records.get(0);
    assertThat(record1.get("resource.id")).isEqualTo(resource1UUID.toString()); // since annotationUUID3 was created last, and it's an annotation on a subresource of resource1

    Map<String, Object> record2 = records.get(1);
    assertThat(record2.get("resource.id")).isEqualTo(resource2UUID.toString());

    List<Map<String, Object>> list1 = (List<Map<String, Object>>) record1.get("_list");
    assertThat(list1).hasSize(2); // 2 annotations for resource1

    Log.info("list1={}", list1);

    Map<String, Object> record11 = list1.get(0);
    assertThat(record11.get("id")).isEqualTo(annotationUUID3.toString());

    Map<String, Object> record12 = list1.get(1);
    assertThat(record12.get("id")).isEqualTo(annotationUUID1.toString());

    List<Map<String, Object>> list2 = (List<Map<String, Object>>) record2.get("_list");
    assertThat(list2).hasSize(1); // 1 annotation for resource1

    Map<String, Object> record21 = list2.get(0);
    assertThat(record21.get("id")).isEqualTo(annotationUUID2.toString());

    // cleanup
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
    String returnType = actualReturnType(method)//
        .getTypeName()//
        .replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "")//
        .replace("Void", "void");
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
    return parameter.getType()//
        .getName()//
        .replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "");
  }

  String parameterName(Parameter parameter) {
    return typeString(parameter).toLowerCase();
  }

}
