package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
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

import java.net.URI;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AboutEntity {
  private URI baseURI;
  private String buildDate;
  private String commitId;
  private String scmBranch;
  private String startedAt;
  private String tentativesTTL;
  private String version;

  public URI getBaseURI() {
    return baseURI;
  }

  public AboutEntity setBaseURI(URI baseURI) {
    this.baseURI = baseURI;
    return this;
  }

  public String getBuildDate() {
    return buildDate;
  }

  public AboutEntity setBuildDate(String buildDate) {
    this.buildDate = buildDate;
    return this;
  }

  public String getCommitId() {
    return commitId;
  }

  public AboutEntity setCommitId(String commitId) {
    this.commitId = commitId;
    return this;
  }

  public String getScmBranch() {
    return scmBranch;
  }

  public AboutEntity setScmBranch(String scmBranch) {
    this.scmBranch = scmBranch;
    return this;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public AboutEntity setStartedAt(String startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  public String getTentativesTTL() {
    return tentativesTTL;
  }

  public AboutEntity setTentativesTTL(String tentativesTTL) {
    this.tentativesTTL = tentativesTTL;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public AboutEntity setVersion(String version) {
    this.version = version;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
