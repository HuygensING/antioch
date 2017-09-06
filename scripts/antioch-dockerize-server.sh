#!/usr/bin/env bash
mvn package && \
  export version=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" pom.xml) && \
  cd antioch-server && \
    docker build -t huygensing/antioch-server -t huygensing/antioch-server:${version} --build-arg version=${version} .
