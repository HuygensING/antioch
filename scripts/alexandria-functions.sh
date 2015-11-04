# usage: source alexandria-functions.sh
# some convenience methods for the shell

function a-annotate-resource {
	r=$1; t=$2; v=$3
	curl -i -X POST $be/resources/$r/annotations -H 'Content-type: application/json' \
    --data-binary "{\"annotation\":{\"type\":\"$t\",\"value\":\"$v\"}}" 2>/dev/null
}

function a-location {
	grep "Location:"|cut -d\  -f2|sed -e "s/\r//g"
}

function a-confirm {
	echo ">> confirming $1 :"
	curl -i -X PUT $1/state -H 'Content-type: application/json' \
    --data-binary '{"state":"CONFIRMED"}'
}

function a-find-annotations-for-resource {
	resource_id=$1
	url=$(curl -i -X POST $be/searches -H 'Content-type: application/json' \
    --data-binary "{\"query\":{
		\"find\" : \"annotation\",
		\"where\" : \"who:eq(\\\"nederlab\\\") resource.id:eq(\\\"${resource_id}\\\")\",
		\"sort\" : \"-when\",
		\"return\" : \"id,when,who,type,value,resource.id,resource.url,subresource.id,subresource.url\",
		\"pageSize\" : 100
    }}" 2>/dev/null|a-location)
    echo "search URI=" $url
    curl ${url}/resultpages/1
}

function a-show-first-resultpage {
	a-location | while read l; do curl -i ${l}/resultPages/1;done
}

function a-generate-random-resource-with-annotation {
  id=$(uuidgen)
  curl -i -X PUT $be/resources/$id -H 'Content-type: application/json' \
  --data-binary "{\"resource\":{
    \"id\":\"$id\",
    \"ref\":\"reference $n\"
  }}"
  url=$(a-annotate-resource "$id" "Tag" "Test annotation for resource $id" | a-location)
  a-confirm $url
}

function a-set-backend {
	export be=$1
	echo -n "backend set to "
	a-show-backend
}

function a-use-localhost {
	a-set-backend http://localhost:2015
}

function a-use-test {
	a-set-backend http://test.alexandria.huygens.knaw.nl/
}

function a-use-acceptance {
	a-set-backend https://acc.alexandria.huygens.knaw.nl/
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
  curl $be/about/service
}
