#!/bin/bash

function usage {
  echo "Usage: deploy-to.sh {test|prod} {base-url}"
}

function divider {
  echo
  echo "--------------------------------------------------------------------------------"
  echo
}

function deploy {
  profile=$1
  base=$2
	mvn package tomcat7:redeploy -P $profile -pl alexandria-webapp -am &&
	(
		divider
		sleep 20 # wait for the server to boot

		curlCmd="curl -sSf $base/about"
		echo $curlCmd
		eval $curlCmd
	)
	divider
}

profile=$1
base=$2
if [[ -n "$profile" && -n "$base" ]]; then
  deploy $profile $base
else
  usage
fi
  
