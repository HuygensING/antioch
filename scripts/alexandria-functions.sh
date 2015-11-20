# usage: source alexandria-functions.sh
# some convenience methods for the shell

function a-annotate-resource {
	r=$1; t=$2; v=$3
	curl -i -X POST -H "${authheader}" $be/resources/$r/annotations -H 'Content-type: application/json' \
    --data-binary "{\"annotation\":{\"type\":\"$t\",\"value\":\"$v\"}}" 2>/dev/null
}

function a-location {
	grep "Location:"|cut -d\  -f2|sed -e "s/\r//g"
}

function a-confirm {
	echo ">> confirming $1 :"
	curl -i -X PUT -H "${authheader}" $1/state -H 'Content-type: application/json' \
    --data-binary '{"state":"CONFIRMED"}'
}

function a-find-annotations-for-resource {
	resource_id=$1
	url=$(curl -i -X POST -H "${authheader}" $be/searches -H 'Content-type: application/json' \
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
  a-generate-resource-with-uuid $id
  url=$(a-annotate-resource "$id" "Tag" "Test annotation for resource $id" | a-location)
  a-confirm $url
}

function a-generate-resource-with-uuid {
  id=$1
  curl -i -X PUT $be/resources/$id -H "${authheader}" -H 'Content-type: application/json' \
  --data-binary "{\"resource\":{
    \"id\":\"$id\",
    \"ref\":\"reference $n\"
  }}"
}


function a-set-text-for-resource-from-file {
  id=$1
  file=$2
  text=$(cat ${file}|sed ':a;N;$!ba;s/\n/\\n/g'|sed -e 's/"/\\"/g') # replace newlines and quotes
  curl -i -X PUT $be/resources/$id/text -H 'Content-type: application/json' \
  --data-binary "{\"text\": {\"body\":\"${text}\"}}" -H "${authheader}"
}

function a-set-text-for-resource-from-xml-file {
  id=$1
  file=$2
  text=$(cat ${file})
  curl -i -X PUT $be/resources/$id/text -H 'Content-type: text/xml' \
  --data-binary "${text}" -H "${authheader}"
}

function a-set-backend {
	export be=$1
	echo -n "backend set to "
	a-show-backend
}

function a-show-backend {
  echo ${be}
}

function a-set-resource-id {
	export id=$1
	echo -n "resource id set to "
	a-show-resource-id
}

function a-set-authkey {
	export authkey=$1
	echo -n "authkey set to ${authkey}"
	export authheader="Auth: SimpleAuth ${authkey}"
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

function a-show-resource-id {
	echo ${id}
}

function a-about {
  curl $be/about
}

function a-about-service {
  curl -H "${authheader}" $be/about/service
}
