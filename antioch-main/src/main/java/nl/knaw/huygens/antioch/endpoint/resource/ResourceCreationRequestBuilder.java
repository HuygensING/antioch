package nl.knaw.huygens.antioch.endpoint.resource;

/*
 * #%L
 * antioch-main
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

import java.util.UUID;

import javax.inject.Singleton;

@Singleton
public class ResourceCreationRequestBuilder {
  public ResourceCreationRequest build(ResourcePrototype protoType) {
    return new ResourceCreationRequest(protoType);
  }

  public SubResourceCreationRequest build(UUID parentId, SubResourcePrototype prototype) {
    return new SubResourceCreationRequest(parentId, prototype);
  }
}
