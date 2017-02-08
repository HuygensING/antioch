package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
