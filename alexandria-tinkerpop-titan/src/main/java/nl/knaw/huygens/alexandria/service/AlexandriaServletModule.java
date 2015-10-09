package nl.knaw.huygens.alexandria.service;

import nl.knaw.huygens.alexandria.config.AbstractAlexandriaServletModule;

public class AlexandriaServletModule extends AbstractAlexandriaServletModule {
  @Override
  public Class<? extends TinkerPopService> getTinkerPopServiceClass() {
    return TitanService.class;
  };
}
