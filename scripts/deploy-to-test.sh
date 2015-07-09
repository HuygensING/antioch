#!/bin/bash
function divider {
  echo
  echo "--------------------------------------------------------------------------------"
  echo
}

base=http://tc23.huygens.knaw.nl/test-alexandria
mvn package tomcat7:redeploy -P test -pl alexandria-webapp -am
divider
sleep 20 # wait for the server to boot
curlCmd="curl -sSf $base/about"
echo $curlCmd
eval $curlCmd