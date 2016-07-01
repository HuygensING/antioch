destinationDir=$1
version=$(grep "<version>" pom.xml |head -1 |cut -d '>' -f 2|cut -d '<' -f 1)
mvn -pl alexandria-server -am package
cp -v alexandria-server/target/alexandria-server-${version}.jar ${destinationDir}/alexandria-server.jar

mvn -pl alexandria-java-client -am package
cp -v alexandria-java-client/target/alexandria-java-client-${version}.jar ${destinationDir}/alexandria-java-client.jar
