package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
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
