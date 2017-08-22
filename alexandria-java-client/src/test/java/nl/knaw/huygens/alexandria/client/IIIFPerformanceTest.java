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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;
import nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants;
import org.glassfish.jersey.client.ChunkedInput;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static nl.knaw.huygens.alexandria.api.ApiConstants.HEADER_AUTH;
import static org.assertj.core.api.Assertions.assertThat;

public class IIIFPerformanceTest extends AlexandriaTestWithTestServer {
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

  static final String authHeader = "SimpleAuth " + AUTHKEY;

  // @Test
  public void testBatchAnnotationUploadOld() {
    int num = 100;
    IIIFAnnotationList list = prepareList(num);

    Stopwatch sw = Stopwatch.createStarted();
    WebTarget rootTarget = client.getRootTarget();
    Response response = rootTarget.path(EndpointPaths.IIIF)//
            .path("identifier")//
            .path("list")//
            .path("name")//
            .path("oldway")//
            .request()//
            .accept(WebAnnotationConstants.JSONLD_MEDIATYPE)//
            .header(HEADER_AUTH, authHeader)//
            .post(Entity.entity(list, WebAnnotationConstants.JSONLD_MEDIATYPE));
    sw.stop();
    long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
    IIIFAnnotationList responseList = response.readEntity(IIIFAnnotationList.class);
    Log.info("NON-STREAMING: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
    assertThat(responseList.getResources()).hasSize(num);
    assertThat(responseList.getResources().get(0).getCreated()).isNotBlank();
    assertThat(responseList.getResources().get(0).getVariablePart().get("@id").toString()).startsWith("http");
  }

  @Test
  public void testBatchAnnotationUploadStreaming() throws IOException {
    int num = 100; // TODO: find out why num=1000 leads to TimeOutException for this test only
    IIIFAnnotationList list = prepareList(num);
    String json = new ObjectMapper().writeValueAsString(list);

    Stopwatch sw = Stopwatch.createStarted();
    WebTarget rootTarget = client.getRootTarget();
    Response response = rootTarget.path(EndpointPaths.IIIF)//
            .path("identifier")//
            .path("list")//
            .path("name")//
            .request()//
            .accept(WebAnnotationConstants.JSONLD_MEDIATYPE)//
            .header(HEADER_AUTH, authHeader)//
            .post(Entity.entity(json, WebAnnotationConstants.JSONLD_MEDIATYPE));
    sw.stop();
    IIIFAnnotationList responseList = response.readEntity(IIIFAnnotationList.class);
    long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
    Log.info("STREAMING: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
    assertThat(responseList.getResources()).hasSize(num);
    assertThat(responseList.getResources().get(0).getCreated()).isNotBlank();
    assertThat(responseList.getResources().get(0).getVariablePart().get("@id").toString()).startsWith("http");
  }

  @Test
  public void testBatchAnnotationUploadChunked() throws IOException {
    int num = 1000;
    IIIFAnnotationList list = prepareList(num);
    String json = new ObjectMapper().writeValueAsString(list);

    Stopwatch sw = Stopwatch.createStarted();
    WebTarget rootTarget = client.getRootTarget();
    Response response = rootTarget.path(EndpointPaths.IIIF)//
            .path("identifier")//
            .path("list")//
            .path("name")//
            .path("chunked")//
            .request()//
            .accept(WebAnnotationConstants.JSONLD_MEDIATYPE)//
            .header(HEADER_AUTH, authHeader)//
            .post(Entity.entity(json, WebAnnotationConstants.JSONLD_MEDIATYPE));
    final ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<ChunkedInput<String>>() {
    });
    StringBuilder jsonBuilder = new StringBuilder();
    String chunk;
    while ((chunk = chunkedInput.read()) != null) {
      System.out.println("<" + chunk);
      jsonBuilder.append(chunk);
    }
    sw.stop();
    String jsonOut = jsonBuilder.toString();
    Log.info("jsonOut={}", jsonOut);
    IIIFAnnotationList responseList = new ObjectMapper().readValue(jsonOut, IIIFAnnotationList.class);
    long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
    Log.info("CHUNKED: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
    assertThat(responseList.getResources()).hasSize(num);
    assertThat(responseList.getResources().get(0).getCreated()).isNotBlank();
    assertThat(responseList.getResources().get(0).getVariablePart().get("@id").toString()).startsWith("http");

  }

  private IIIFAnnotationList prepareList(int num) {
    IIIFAnnotationList list = new IIIFAnnotationList();
    list.setContext("http://iiif.io/api/presentation/2/context.json");
    list.putKeyValue("@type", "sc:AnnotationList");
    List<WebAnnotationPrototype> resources = new ArrayList<>(num);
    for (int i = 0; i < num; i++) {
      WebAnnotationPrototype webannotation = new WebAnnotationPrototype();
      webannotation.putKeyValue("@type", "sc:Annotation");

      String[] motivation = new String[]{"oa:tagging", "oa:commenting"};
      webannotation.putKeyValue("motivation", motivation);

      List<Map<String, String>> resourceList = new ArrayList<>(2);
      resourceList.add(ImmutableMap.of(//
              "@type", "oa:Tag", //
              "chars", "Square"//
      ));
      resourceList.add(ImmutableMap.of(//
              "@type", "dctypes:Text", //
              "format", "text/html", //
              "chars", "<p>text " + i + "</p>"//
      ));
      webannotation.putKeyValue("resource", resourceList);
      webannotation.putKeyValue("on", "https://purl.stanford.edu/wh234bz9013/iiif/canvas-0#xywh=1,2,3," + i);

      resources.add(webannotation);
    }
    list.setResources(resources);
    return list;
  }
}
