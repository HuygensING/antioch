package nl.knaw.huygens.alexandria.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import java.util.concurrent.TimeUnit;

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

  @Test
  public void testBatchAnnotationUploadOld() {
    int num = 100;
    String json = prepareLoad(num);

    Stopwatch sw = new Stopwatch();
    sw.start();
    WebTarget rootTarget = client.getRootTarget();  
    rootTarget.path(EndpointPaths.IIIF).request().p

    sw.stop();
    long elapsed = sw.elapsed(TimeUnit.SECONDS);
    Log.info("Uploading a batch of {} annotations took {} seconds.", num, elapsed);

  }

  private String prepareLoad(int num) {
//    JsonFactory jf = new JsonFactory();
//    jf.createParser()
//    jf.createGenerator(out)
  }
}
