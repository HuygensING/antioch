package nl.knaw.huygens.alexandria;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

@Singleton
public class MyServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  AlexandriaConfiguration config;

  @Inject
  public MyServlet(AlexandriaConfiguration config) {
    this.config = config;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.getWriter().println("Service: " + config.getStorageDirectory());
  }
}
