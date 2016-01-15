package nl.knaw.huygens.alexandria.jaxrs;

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

import java.util.HashMap;
import java.util.Map;

import nl.knaw.huygens.Log;

public class ThreadContext {
  private static final String DEFAULT_USERNAME = "nederlab"; // TODO: remove the need for this.
  private static ThreadLocal<Map<String, Object>> threadLocalMap = new ThreadLocal<>();
  private static ThreadLocal<String> threadLocalUsername = new ThreadLocal<>();

  public static void put(String key, Object value) {
    getMap().put(key, value);
  }

  public static Object get(String key) {
    return getMap().get(key);
  }

  public static String getUserName() {
    String name = threadLocalUsername.get();
    return name != null ? name : DEFAULT_USERNAME;
  }

  public static void setUserName(String username) {
    threadLocalUsername.set(username);
  }

  @Override
  protected void finalize() throws Throwable {
    Log.info("Session.finalize()");
    super.finalize();
    threadLocalMap.remove();
    threadLocalUsername.remove();
  }

  private static Map<String, Object> getMap() {
    Map<String, Object> inner = threadLocalMap.get();
    if (inner == null) {
      inner = new HashMap<>();
      threadLocalMap.set(inner);
    }
    return inner;
  }
}
