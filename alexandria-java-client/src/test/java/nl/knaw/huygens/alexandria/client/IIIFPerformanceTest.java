package nl.knaw.huygens.alexandria.client;

import static nl.knaw.huygens.alexandria.api.ApiConstants.HEADER_AUTH;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ChunkedInput;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;
import nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants;

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

  @Test
  public void testBatchAnnotationUploadOld() {
    int num = 2000;
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
    response.bufferEntity();
    Log.info("NON-STREAMING: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
  }

  // @Test
  // public void testBatchAnnotationUploadStreaming() throws IOException {
  // int num = 200;
  // IIIFAnnotationList list = prepareList(num);
  // String json = new ObjectMapper().writeValueAsString(list);
  //
  // Stopwatch sw = Stopwatch.createStarted();
  // WebTarget rootTarget = client.getRootTarget();
  // Response response = rootTarget.path(EndpointPaths.IIIF)//
  // .path("identifier")//
  // .path("list")//
  // .path("name")//
  // .path("streaming")//
  // .request()//
  // .accept(WebAnnotationConstants.JSONLD_MEDIATYPE)//
  // .header(HEADER_AUTH, authHeader)//
  // .post(Entity.entity(json, WebAnnotationConstants.JSONLD_MEDIATYPE));
  // sw.stop();
  // InputStream cris = (InputStream) response.getEntity();
  // IIIFAnnotationList responseList = new ObjectMapper().readValue(cris, IIIFAnnotationList.class);
  // assertThat(responseList.getResources()).hasSize(num);
  // long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
  // Log.info("STREAMING: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
  // }

  @Test
  public void testBatchAnnotationUploadChunked() throws IOException {
    int num = 2000;
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
    assertThat(responseList.getResources()).hasSize(num);
    long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
    Log.info("CHUNKED: Uploading a batch of {} annotations took {} milliseconds.\nresponse: {}", num, elapsed, response);
  }

  private IIIFAnnotationList prepareList(int num) {
    IIIFAnnotationList list = new IIIFAnnotationList();
    list.setContext("http://iiif.io/api/presentation/2/context.json");
    list.putKeyValue("@type", "sc:AnnotationList");
    List<WebAnnotationPrototype> resources = new ArrayList<>(num);
    for (int i = 0; i < num; i++) {
      WebAnnotationPrototype webannotation = new WebAnnotationPrototype();
      webannotation.putKeyValue("@type", "sc:Annotation");

      String[] motivation = new String[] { "oa:tagging", "oa:commenting" };
      webannotation.putKeyValue("motivation", motivation);

      List<Map<String, String>> resourceList = new ArrayList<>(2);
      resourceList.add(ImmutableMap.<String, String> of(//
          "@type", "oa:Tag", //
          "chars", "Square"//
      ));
      resourceList.add(ImmutableMap.<String, String> of(//
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
