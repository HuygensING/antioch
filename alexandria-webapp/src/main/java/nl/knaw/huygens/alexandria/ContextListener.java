package nl.knaw.huygens.alexandria;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;

@WebListener
public class ContextListener extends JerseyGuiceServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    Log.info("ContextListener.contextInitialized()");
    super.contextInitialized(servletContextEvent);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    Log.info("ContextListener.contextDestroyed()");
    super.contextDestroyed(servletContextEvent);
  }

  @Override
  protected List<? extends Module> modules() {
    Log.info("modules() called");
    return ImmutableList.of(new AlexandriaServletModule());
  }
}