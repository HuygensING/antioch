package nl.knaw.huygens.alexandria;

import com.google.inject.AbstractModule;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class TestConfigurationModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AlexandriaConfiguration.class).to(TestConfiguration.class);
  }

}
