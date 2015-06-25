package nl.knaw.huygens.alexandria.endpoint;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public interface CreationRequest<T> {

  public T execute(AlexandriaService service);

}