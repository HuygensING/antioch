#!/usr/bin/env bash
destinationDir=$1
version=$(grep "<version>" pom.xml |head -1 |cut -d '>' -f 2|cut -d '<' -f 1)
mvn -pl antioch-server -am package
cp -v antioch-server/target/antioch-server-${version}.jar ${destinationDir}/antioch-server.jar

mvn -pl antioch-java-client -am package
cp -v antioch-java-client/target/antioch-java-client-${version}.jar ${destinationDir}/antioch-java-client.jar
