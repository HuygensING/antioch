package nl.knaw.huygens.alexandria.jaxrs;

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

import java.util.HashMap;
import java.util.Map;

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
    // Log.info("Session.finalize()");
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
