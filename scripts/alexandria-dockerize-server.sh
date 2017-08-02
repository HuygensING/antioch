#!/usr/bin/env bash
mvn package && \
  export version=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" pom.xml) && \
  cd alexandria-server && \
    docker build -t huygensing/alexandria-server -t huygensing/alexandria-server:${version} --build-arg version=${version} .
