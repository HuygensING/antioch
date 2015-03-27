package nl.knaw.huygens.alexandria.resource;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreationFixture extends ResourceFixture {
  private static final Logger LOG = LoggerFactory.getLogger(CreationFixture.class);

  @Override
  public void request(String method, String path) {
    LOG.trace("CreationFixture.request({},{})", method, path);

    when(resourceService().createResource(any(AlexandriaResource.class))).then(returnsFirstArg());

    super.request(method, path);
  }

}
