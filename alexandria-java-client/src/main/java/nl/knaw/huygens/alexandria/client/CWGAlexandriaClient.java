package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;

public class CWGAlexandriaClient extends OptimisticAlexandriaClient {

  public CWGAlexandriaClient(URI alexandriaURI) {
    super(alexandriaURI);
  }

  public CWGAlexandriaClient(URI alexandriaURI, SSLContext sslContext) {
    super(alexandriaURI, sslContext);
  }

  public List<UUID> findChildResourcesByRef(UUID parentUUID, String ref) throws AlexandriaException {
    AlexandriaQuery query = new AlexandriaQuery()//
        .setFind("resource")//
        .setWhere(MessageFormat.format("subresource.sub:eq(\"{0}\") resource.id:eq(\"{1}\")", ref, parentUUID))//
        .setReturns(QueryField.subresource_id.toString());
    UUID searchId = addSearch(query);
    SearchResultPage searchResultPage = getSearchResultPage(searchId);
    return searchResultPage.getRecords()//
        .stream()//
        .map(r -> r.get(QueryField.subresource_id.toString()))//
        .map(String.class::cast)//
        .map(UUID::fromString)//
        .collect(toList());
  }

}
