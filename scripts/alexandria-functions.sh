# usage: source alexandria-functions.sh
# some convenience methods for the shell

function a-annotate-resource {
  r=$1; t=$2; v=$3
  curl -i -X POST --header "${authheader}" $be/resources/$r/annotations --header 'Content-type: application/json' \
    --data-binary "{\"annotation\":{\"type\":\"$t\",\"value\":\"$v\"}}" 2>/dev/null
}

function a-location {
  grep "Location:"|cut -d\  -f2|sed -e "s/\r//g"|sed -e "s/https:\/\/acc.alexandria.huygens.knaw.nl/http:\/\/tc24alex.huygens.knaw.nl\/alexandria/g"
}

function a-confirm {
  echo ">> confirming $1 :"
  curl -i -X PUT --header "${authheader}" $1/state --header 'Content-type: application/json' \
    --data-binary '{"state":"CONFIRMED"}'
}

function a-find-annotations-for-resource {
  resource_id=$1
  url=$(curl -i -X POST --header "${authheader}" $be/searches --header 'Content-type: application/json' \
    --data-binary "{\"query\":{
    \"find\" : \"annotation\",
    \"where\" : \"resource.id:eq(\\\"${resource_id}\\\")\",
    \"sort\" : \"-when\",
    \"return\" : \"id,when,who,type,value,resource.id,resource.url,subresource.id,subresource.url\",
    \"pageSize\" : 100
    }}" 2>/dev/null|a-location)
    echo "search URI=" $url
    curl ${url}/resultpages/1
}

function a-show-first-resultpage {
  a-location | while read l; do curl -i ${l}/pages/1;done
}

function a-generate-random-resource-with-annotation {
  id=$(uuidgen)
  a-generate-resource-with-uuid $id
  url=$(a-annotate-resource "$id" "Tag" "Test annotation for resource $id" | a-location)
  a-confirm $url
}

function a-test {
  id=$(uuidgen)
  a-generate-resource-with-uuid $id
  url=$(a-annotate-resource "$id" "Tag" "Test annotation for resource $id" | a-location)
  a-confirm $url
  a-delete $url  
}

function a-generate-resource-with-uuid {
  id=$1
  curl -i -X PUT $be/resources/$id --header "${authheader}" --header 'Content-type: application/json' \
  --data-binary "{\"resource\":{
    \"id\":\"$id\",
    \"ref\":\"reference $n\"
  }}"
}

function a-generate-confirmed-subresource-with-title {
  suburi=$(curl -i --header "${authheader}" -X POST $be/resources/$ri/subresources --header 'Content-type: application/json' \
    --data-binary '{"subresource":{ "sub":"$1" }}' | a-location )
  a-confirm ${suburi}
  echo ${suburi}
}

function a-set-default-baselayer-definition {
  echo "Setting default baselayer definition for ${be}/resources/$ri"
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/baselayerdefinition -H 'Content-type: application/json' \
     --data-binary '{"baseLayerDefinition":{
       "baseElementDefinitions" : [
          { "name": "text", "baseAttributes": [ "id" ] },
          { "name": "p", "baseAttributes": [ "id" ] },
          { "name": "div", "baseAttributes" : [ "id", "by" ] }
       ]}
     }'
}

function a-set-text-from-file {
  echo "Setting resource text for ${be}/resources/$ri"
  curl -i --header "${authheader}" -X PUT ${be}/resources/${ri}/text --header 'Content-Type:application/octet-stream' --data @"$*"
}

function a-delete {
  curl -i -X DELETE --header "${authheader}" $1
}

function a-set-backend {
  export be=$1
  echo -n "backend set to "
  a-show-backend
}

function a-set-resource-id {
  export ri=$1
}

function a-set-authkey {
  export authkey=$1
  echo -n "authkey set to ${authkey}"
  export authheader="Auth: SimpleAuth ${authkey}"
}

function a-use-localhost {
  a-set-backend http://localhost:2015
  a-set-authkey YHJZHjpke8JYjm5y
}

function a-use-test {
  a-set-backend http://test.alexandria.huygens.knaw.nl/
}

function a-use-acceptance {
  a-set-backend http://tc24alex.huygens.knaw.nl/alexandria
  a-set-authkey YHJZHjpke8JYjm5y
}

function a-use-production {
  a-set-backend https://alexandria.huygens.knaw.nl/
}

function a-show-backend {
  echo ${be}
}

function a-about {
  curl $be/about
}

function a-about-service {
  curl --header "${authheader}" $be/about/service
}

function a-dry-run {
  ri=$(uuidgen)
  a-generate-resource-with-uuid $ri
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/baselayerdefinition -H 'Content-type: application/json' \
	--data-binary '{
	  "baseLayerDefinition": {
	    "baseElementDefinitions": [ {
	      "name": "body"
	    }, {
	      "name": "div",
	      "baseAttributes": [ "type" ]
	    }, {
	      "name": "p"
	    }, {
	      "name": "sub"
	    }, {
	      "name": "sup"
	    } ]
	  }
	}'
	echo
  echo ">> result uploading text:"
  curl --silent --header "${authheader}" -X PUT ${be}/resources/${ri}/text --header 'Content-Type:text/xml' --data "$*"|jq "."
	echo
  echo ">> extracted baselayer:"
  curl ${be}/resources/${ri}/text
}

a-use-localhost

