function divider {
  echo
  echo "--------------------------------------------------------------------------------"
  echo
}

cd alexandria-webapp && \
mvn clean tomcat:redeploy -P test
divider
sleep 20 # wait for the server to boot
curlCmd="curl -sSf http://demo17.huygens.knaw.nl/test-alexandria/about"
echo $curlCmd
