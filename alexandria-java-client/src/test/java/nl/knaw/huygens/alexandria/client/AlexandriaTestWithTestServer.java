package nl.knaw.huygens.alexandria.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

public abstract class AlexandriaTestWithTestServer extends AlexandriaTest {
  static final String AUTHKEY = "AUTHKEY";

  protected static URI testURI = URI.create("http://localhost:2016/");
  private static HttpServer testServer;
  private static AlexandriaConfiguration testConfig = new AlexandriaConfiguration() {
    @Override
    public String getStorageDirectory() {
      return System.getProperty("java.io.tmpdir");
    }

    @Override
    public URI getBaseURI() {
      return testURI;
    }

    @Override
    public Map<String, String> getAuthKeyIndex() {
      return ImmutableMap.of("AUTHKEY", "testuser");
    }

    @Override
    public String getAdminKey() {
      return "adminkey";
    }

    @Override
    public Boolean asynchronousEndpointsAllowed() {
      return true;
    }
  };

  private static TinkerPopService tinkerpopService;

  @BeforeClass
  public static void startTestServer() {
    final ServiceLocator locator = createServiceLocator();
    final AlexandriaService service = locator.getService(AlexandriaService.class);
    final ResourceConfig resourceConfig = new AlexandriaApplication();
    tinkerpopService = ((TinkerPopService) service);
    tinkerpopService.setStorage(new Storage(TinkerGraph.open()));
    testServer = GrizzlyHttpServerFactory.createHttpServer(testURI, resourceConfig, locator);
  }

  @AfterClass
  public static void stopTestServer() {
    testServer.shutdown();
  }

  private static ServiceLocator createServiceLocator() {
    final ServiceLocator locator = BootstrapUtils.newServiceLocator();
    final AbstractModule configModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(testConfig);
      }
    };
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
    BootstrapUtils.install(locator);
    return locator;
  }

  void dumpDb() {
    Log.info("dumping server graph as graphSON:");
    try {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      tinkerpopService.dumpToGraphSON(outputstream);
      outputstream.flush();
      outputstream.close();
      String[] lines = outputstream.toString("UTF8").split("\n");
      ObjectMapper mapper = new ObjectMapper();
      for (String json : lines) {
        Object object = mapper.readValue(json, Object.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.info("dumping done");
  }

  void dumpDb1() {
    Log.info("dumping server graph as graphML:");
    try {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      tinkerpopService.dumpToGraphML(outputstream);
      outputstream.flush();
      outputstream.close();
      System.out.println(prettyFormat(outputstream.toString()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.info("dumping done");
  }

  static String prettyFormat(String input, int indent) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      // transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    } catch (Exception e) {
      throw new RuntimeException(e); // simple exception handling, please review it
    }
  }

  static String prettyFormat(String input) {
    return prettyFormat(input, 2);
  }
}
