package nl.knaw.huygens.alexandria;

import javax.servlet.ServletContextEvent;

import nl.knaw.huygens.Log;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class ContextListener extends GuiceServletContextListener {
  public static Injector injector;

  public ContextListener() {
    injector = Guice.createInjector(new WebServletModule());
  }

  @Override
  protected Injector getInjector() {
    Log.info("ContextListener.getInjector()");
    return injector;
  }

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
}