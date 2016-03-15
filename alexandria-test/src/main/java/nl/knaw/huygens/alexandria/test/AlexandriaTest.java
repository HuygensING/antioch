package nl.knaw.huygens.alexandria.test;

/*
 * #%L
 * alexandria-test
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

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;

public abstract class AlexandriaTest {

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  protected String singleQuotesToDouble(String stringWithSingleQuotes) {
    return stringWithSingleQuotes.replace("'", "\"");
  }

}
