package nl.knaw.huygens.alexandria.endpoint.homepage;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;

@Singleton
@Path("")
public class HomePageEndpoint extends JSONEndpoint {
  /**
   * Shows the homepage for the backend
   *
   * @return HTML representation of the homepage
   * @throws IOException
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getHomePage() throws IOException {
    InputStream resourceAsStream = Thread.currentThread()//
        .getContextClassLoader().getResourceAsStream("index.html");
    return Response//
        .ok(resourceAsStream)//
        .header("Pragma", "public")//
        .header("Cache-Control", "public")//
        .build();

  }

  @GET
  @Path("favicon.ico")
  public Response getFavIcon() {
    return noContent();
  }

  @GET
  @Path("robots.txt")
  public String noRobots() {
    return "User-agent: *\nDisallow: /\n";
  }
}
