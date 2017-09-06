package nl.knaw.huygens.antioch.client;

/*
 * #%L
 * antioch-java-client
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

public class AnnotatorsTest extends AntiochClientTest {
  // TODO move to markup module
  // @Before
  // public void before() {
  // client.setAuthKey(AUTHKEY);
  // client.setAutoConfirm(true);
  // }
  //
  // @Test
  // public void testSetThenGetAnnotator() {
  // String resourceRef = "test";
  // UUID resourceUuid = createResource(resourceRef);
  // String code = "abc";
  // String description = "Annotator description";
  // setResourceAnnotator(resourceUuid, code, description);
  //
  // RestResult<Annotator> getResult = client.getAnnotator(resourceUuid, code);
  // assertRequestSucceeded(getResult);
  // Annotator annotator2 = getResult.get();
  // assertThat(annotator2.getCode()).isEqualTo(code);
  // assertThat(annotator2.getDescription()).isEqualTo(description);
  // assertThat(annotator2.getResourceURI()).hasToString("http://localhost:2016/resources/" + resourceUuid);
  // }
  //
  // private void setResourceAnnotator(UUID resourceUuid, String code, String description) {
  // Annotator annotator = new Annotator().setDescription(description);
  // RestResult<Void> putResult = client.setAnnotator(resourceUuid, code, annotator);
  // assertRequestSucceeded(putResult);
  // // URI uri = putResult.get();
  // // assertThat(uri).hasToString("http://localhost:2016/resources/" + resourceUuid + "/annotators/abc");
  // }
  //
  // @Test
  // public void testAnnotators() {
  // String resourceRef = "test";
  // UUID resourceUUID = createResource(resourceRef);
  // AnnotatorList annotatorList = getAnnotatorList(resourceUUID);
  // assertThat(annotatorList).isEmpty();
  //
  // UUID subresourceUUID = createSubResource(resourceUUID, "ref");
  // String code = "abc";
  // String description = "Annotator abc";
  // setResourceAnnotator(resourceUUID, code, description);
  // AnnotatorList annotatorList2 = getAnnotatorList(subresourceUUID);
  // assertThat(annotatorList2).hasSize(1);
  // Annotator annotator = annotatorList2.get(0);
  // assertThat(annotator.getCode()).isEqualTo(code);
  // assertThat(annotator.getDescription()).isEqualTo(description);
  // }
  //
  // private AnnotatorList getAnnotatorList(UUID resourceUUID) {
  // RestResult<AnnotatorList> result = client.getAnnotators(resourceUUID);
  // assertRequestSucceeded(result);
  // return result.get();
  // }

}
