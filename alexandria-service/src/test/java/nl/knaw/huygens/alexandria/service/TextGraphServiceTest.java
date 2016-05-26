package nl.knaw.huygens.alexandria.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.junit.Before;

import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;

public class TextGraphServiceTest extends AlexandriaTest {

  private static final LocationBuilder LOCATION_BUILDER = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());
  private Storage storage = new Storage(TinkerGraph.open());
  private TinkerPopService service = new TinkerGraphService(LOCATION_BUILDER);

  @Before
  public void before() {
    service.setStorage(storage);
  }

  // @Test
  public void testUpdateTextAnnotationLink() {
    UUID resourceUUID = UUID.randomUUID();
    String xml = "<text><p xml:id=\"p-1\">This is a paragraph.</p></text>";
    createResourceWithText(resourceUUID, xml);

    TextRangeAnnotationVF vf = storage.createVF(TextRangeAnnotationVF.class);
    Position position = new Position()//
        .setXmlId("p-1")//
        .setOffset(6)//
        .setLength(2);
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(UUID.randomUUID())//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    TextGraphService tgs = new TextGraphService(storage);

    dumpDb();
    tgs.updateTextAnnotationLink(vf, textRangeAnnotation, resourceUUID);
    dumpDb();

  }

  private void createResourceWithText(UUID resourceUUID, String xml) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceUUID, provenance);
    service.createOrUpdateResource(resource);
    ParseResult parseresult = TextGraphUtil.parse(xml);
    service.storeTextGraph(resourceUUID, parseresult);
  }

  void dumpDb() {
    Log.info("dumping server graph as graphML:");
    try {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      service.dumpToGraphML(outputstream);
      outputstream.flush();
      outputstream.close();
      System.out.println(outputstream.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.info("dumping done");
  }

}
