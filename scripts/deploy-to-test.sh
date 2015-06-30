function divider {
  echo
  echo "--------------------------------------------------------------------------------"
  echo
}

base=http://demo17.huygens.knaw.nl/test-alexandria
mvn package tomcat:redeploy -P test -pl alexandria-webapp -am
divider
sleep 20 # wait for the server to boot
curlCmd="curl -sSf $base/about"
echo $curlCmd
