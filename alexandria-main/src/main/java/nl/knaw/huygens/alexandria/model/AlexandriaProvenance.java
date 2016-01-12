package nl.knaw.huygens.alexandria.model;

/*
 * #%L
 * alexandria-main
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

import java.time.Instant;

public class AlexandriaProvenance {
  public static final String DEFAULT_WHY = "";
  // public static final String DEFAULT_WHO = "nederlab";

  private final String who;
  private final Accountable what;
  private final Instant when;
  private final String why;

  public AlexandriaProvenance(Accountable what, String who, Instant when, String why) {
    this.who = who;
    this.what = what;
    this.when = when;
    this.why = why;
  }

  public String getWho() {
    return who;
  }

  public Accountable getWhat() {
    return what;
  }

  public Instant getWhen() {
    return when;
  }

  public String getWhy() {
    return why;
  }

}
