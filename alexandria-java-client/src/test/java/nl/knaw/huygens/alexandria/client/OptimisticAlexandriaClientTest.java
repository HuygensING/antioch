package nl.knaw.huygens.alexandria.client;

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

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
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
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

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

  /// end tests

  protected UUID createResource(String resourceRef) {
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    UUID resourceUuid = UUID.randomUUID();
    client.setResource(resourceUuid, resource);
    return resourceUuid;
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
