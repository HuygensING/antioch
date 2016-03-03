package nl.knaw.huygens.alexandria.gutenberg;

/*
 * #%L
 * alexandria-performance-tests
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class PgTerms {

  private static final Model memoryModel = ModelFactory.createDefaultModel();

  public static final String NS = "http://www.gutenberg.org/2009/pgterms/";

  public static Property name = memoryModel.createProperty(NS, "name");

  public static Property downloads = memoryModel.createProperty(NS, "downloads");

}
