#!/bin/bash

function usage {
  echo "Usage: deploy-to.sh {test|production}"
}

function divider {
  echo
  echo "--------------------------------------------------------------------------------"
  echo
}

function deploy {
  if [ "test" == "$1" ]; then
    base=http://tc23.huygens.knaw.nl/test-alexandria
    profile=test
	  
	elif [ "production" == "$1" ]; then
    base=http://tc23.huygens.knaw.nl/alexandria
    profile=prod
	
	else
	  usage
	  exit
	fi

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

deploy $1