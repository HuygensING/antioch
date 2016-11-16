# usage: source alexandria-functions.sh
# some convenience methods for the shell

function a-log {
	echo
	echo ">> $*"
	echo
}

function a-annotate-resource {
  r=$1; t=$2; v=$3
  curl -i -X POST --header "${authheader}" ${be}/resources/${r}/annotations --header 'Content-type: application/json' \
    --data-binary "{\"annotation\":{\"type\":\"$t\",\"value\":\"$v\"}}" 2>/dev/null
}

function a-location {
  grep "Location:"|cut -d\  -f2|tr -d '\r' |sed -e "s/https:\/\/acc.alexandria.huygens.knaw.nl/http:\/\/tc24alex.huygens.knaw.nl\/alexandria/g"
}

function a-confirm {
  a-log "confirming $1 :"
  curl -i -X PUT --header "${authheader}" $1/state --header 'Content-type: application/json' \
    --data-binary '{"state":"CONFIRMED"}'
}

function a-find-annotations-for-resource {
  resource_id=$1
  url=$(curl -i -X POST --header "${authheader}" ${be}/searches --header 'Content-type: application/json' \
    --data-binary "{\"query\":{
    \"find\" : \"annotation\",
    \"where\" : \"resource.id:eq(\\\"${resource_id}\\\")\",
    \"sort\" : \"-when\",
    \"return\" : \"id,when,who,type,value,resource.id,resource.url,subresource.id,subresource.url\",
    \"pageSize\" : 100
    }}" 2>/dev/null|a-location)
  echo "search URI=" ${url}
  curl ${url}/pages/1
}

function a-show-first-resultpage {
  a-location | while read l; do curl -i ${l}/pages/1;done
}

function a-generate-random-resource-with-annotation {
  id=$(uuidgen)
  a-generate-resource-with-uuid ${id}
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

function a-generate-resource-with-uuid-and-ref {
  id=$1
  ref=$2
  curl -i -X PUT $be/resources/$id --header "${authheader}" --header 'Content-type: application/json' \
  --data-binary "{\"resource\":{
    \"id\":\"$id\",
    \"ref\":\"${ref}\"
  }}"
}

function a-generate-confirmed-subresource-with-title {
  suburi=$(curl -i --header "${authheader}" -X POST $be/resources/$ri/subresources --header 'Content-type: application/json' \
    --data-binary '{"subresource":{ "sub":"$1" }}' | a-location )
  a-confirm ${suburi}
  echo ${suburi}
}

function a-set-baselayer-textview {
  a-log "Setting default baselayer textview for ${be}/resources/$ri"
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/text/views/baselayer -H 'Content-type: application/json' \
     --data-binary '{"textView":{
       "description" : "The base layer",
     	 "ignoredElements": ["note"],
       "includedElements" : [
          { "name": "text", "includedAttributes": [ "id" ] },
          { "name": "p", "includedAttributes": [ "id" ] },
          { "name": "div", "includedAttributes" : [ "id", "by" ] }
       ]}
     }'
}

function a-set-text-from-file {
  a-log "Setting resource text for ${be}/resources/$ri"
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
  a-set-authkey ${ALEXANDRIA_AUTHKEY_LOCAL}
}

function a-use-localip {
  localip=$(ipconfig|grep IPv4|grep -v Autoconfiguration|sed -e "s/.*: //")
  a-set-backend http://${localip}:2015
  a-set-authkey ${ALEXANDRIA_AUTHKEY_LOCAL}
}

function a-use-test {
  a-set-backend http://test.alexandria.huygens.knaw.nl/
  a-set-authkey ${ALEXANDRIA_AUTHKEY_TEST}
}

function a-use-acceptance {
  a-set-backend http://tc24alex.huygens.knaw.nl/alexandria
  a-set-authkey ${ALEXANDRIA_AUTHKEY_ACC}
}

function a-use-production {
  a-set-backend https://alexandria.huygens.knaw.nl/
  a-set-authkey ${ALEXANDRIA_AUTHKEY_PROD}
}

function a-show-backend {
  a-log ${be}
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
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/text/views/baselayer -H 'Content-type: application/json' \
  --data-binary '{
    "textView": {
      "description" : "The Base Layer",
      "ignoredElements": ["note"],
      "includedElements": [ {
        "name": "body"
      }, {
        "name": "div",
        "includedAttributes": [ "type" ]
      }, {
        "name": "text"
      }, {
        "name": "p"
      }, {
        "name": "table"
      }, {
        "name": "row"
      }, {
        "name": "cell"
      }, {
        "name": "sub"
      }, {
        "name": "sup"
      } ]
    }
  }'
  a-log "result uploading text:"
  curl --silent --header "${authheader}" -X PUT ${be}/resources/${ri}/text --header 'Content-Type:text/xml' --data "$*" | jq "."
  a-log "extracted baselayer:"
  curl ${be}/resources/${ri}/text/xml?view=baselayer
}

function a-dry-run-from-file {
  ri=$(uuidgen)
  a-generate-resource-with-uuid $ri
  a-log "result uploading text:"
  location=$(curl -i --header "${authheader}" -X PUT ${be}/resources/${ri}/text --header 'Content-Type:application/octet-stream' --data @"$*" |a-location)
  a-log "Location: ${location}"
  curl --silent ${location} | jq "."
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/text/views/baselayer -H 'Content-type: application/json' \
  --data-binary '{
    "textView": {
      "description" : "The Base Layer",
      "ignoredElements": ["note"],
      "includedElements": [ {
        "name": "body"
      }, {
        "name": "div",
        "includedAttributes": [ "type" ]
      }, {
        "name": "text"
      }, {
        "name": "p"
      }, {
        "name": "table"
      }, {
        "name": "row"
      }, {
        "name": "cell"
      }, {
        "name": "sub"
      }, {
        "name": "sup"
      } ]
    }
  }'
  a-log "extracted baselayer:"
  curl ${be}/resources/${ri}/text/views/baselayer
  curl --silent ${location} | jq "."
  a-log "see status at ${location}"
}

function a-gutenberg-import-file {
  title=$1
  shift
  a-log ${title}
  ri=$(uuidgen)
  a-generate-resource-with-uuid-and-ref $ri "gutenberg:${title}"
  curl -i -H "${authheader}" -X PUT $be/resources/$ri/baselayerdefinition -H 'Content-type: application/json' \
  --data-binary '{
    "baseLayerDefinition": {
      "subresourceElements": ["note"],
      "baseElements": [ {
        "name": "TEI"
      }, {
        "name": "div",
        "includedAttributes": [ "type" ]
      }, {
        "name": "body"
      }, {
        "name": "p"
      }, {
        "name": "sub"
      }, {
        "name": "sup"
      }, {
        "name": "lb"
      } ]
    }
  }'
  a-log "result uploading text:"
  location=$(curl -i --header "${authheader}" -X PUT ${be}/resources/${ri}/text --header 'Content-Type:application/octet-stream' --data @"$*" |a-location)
  a-log "Location: ${location}"
  curl --silent ${location} | jq "."
  a-log "see status at ${location}"
}

function a-import-gryo-dump {
  importfile=$1
  if [[ ! -e ${importfile} ]]; then
    echo "importfile ${importfile} not found"
  else
    curl -i -H "${authheader}" -X PUT ${be}/admin -H 'Content-type: application/json' \
    --data-binary "{
      \"key\" : \"${authkey}\",
      \"command\" : \"import\",
      \"parameters\" : {
        \"format\" : \"gryo\",
        \"filename\" : \"${importfile}\"
      }
    }}"
  fi
}

function a-import-gryo-dump {
  importfile=$1
  if [[ ! -e ${importfile} ]]; then
    echo "importfile ${importfile} not found"
  else
    curl -i -H "${authheader}" -X PUT ${be}/admin -H 'Content-type: application/json' \
    --data-binary "{
      \"key\" : \"${authkey}\",
      \"command\" : \"import\",
      \"parameters\" : {
        \"format\" : \"graphml\",
        \"filename\" : \"${importfile}\"
      }
    }}"
  fi
}

function a-import-graphson-dump {
  importfile=$1
  if [[ ! -e ${importfile} ]]; then
    echo "importfile ${importfile} not found"
  else
    curl -i -H "${authheader}" -X PUT ${be}/admin -H 'Content-type: application/json' \
    --data-binary "{
      \"key\" : \"${authkey}\",
      \"command\" : \"import\",
      \"parameters\" : {
        \"format\" : \"graphson\",
        \"filename\" : \"${importfile}\"
      }
    }}"
  fi
}

function a-get-web-annotation {
  uuid=$1
  curl -i \
    -H 'Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"' \
    ${be}/webannotations/${uuid}
}


function a-delete-web-annotation {
  uuid=$1
  curl -i \
    -X DELETE \
    -H "${authheader}" \
    ${be}/webannotations/${uuid}
}

function a-add-web-annotation {
  curl \
    -H "${authheader}" \
    -H 'Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"' \
    -H 'Content-type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"' \
    --data-binary "$1" \
    ${be}/webannotations | jq "."
}

function a-update-web-annotation {
  curl \
    -X PUT \
    -H "${authheader}" \
    -H 'Accept: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"' \
    -H 'Content-type: application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"' \
    --data-binary "$2" \
    ${be}/webannotations/$1 | jq "."
}


function a-add-sample-web-annotation1 {
	a-add-web-annotation '{
  "@context": "http://iiif.io/api/presentation/2/context.json",
  "@type": "oa:Annotation",
  "motivation": [
    "oa:tagging",
    "oa:commenting"
  ],
  "resource": [
    {
      "@type": "oa:Tag",
      "chars": "TestTag"
    },
    {
      "@type": "dctypes:Text",
      "format": "text/html",
      "chars": "<p>TestAnnotatie<\/p>"
    }
  ],
  "on": {
    "@type": "oa:SpecificResource",
    "full": "https://iiif.lib.harvard.edu/manifests/drs:5981093/canvas/canvas-5981120.json",
    "selector": {
      "@type": "oa:SvgSelector",
      "value": "<svg xmlns=\"http://www.w3.org/2000/svg\"><path xmlns=\"http://www.w3.org/2000/svg\" d=\"M2544.65571,871.97372l539.74212,0l0,0l539.74212,0l0,331.89112l0,331.89112l-539.74212,0l-539.74212,0l0,-331.89112z\" data-paper-data=\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\" id=\"rectangle_0403bd67-c6af-4b48-b5bf-f845fd5b5944\" fill-opacity=\"0\" fill=\"#00bfff\" fill-rule=\"nonzero\" stroke=\"#00bfff\" stroke-width=\"6.70487\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"10\" stroke-dasharray=\"\" stroke-dashoffset=\"0\" font-family=\"sans-serif\" font-weight=\"normal\" font-size=\"12\" text-anchor=\"start\" style=\"mix-blend-mode: normal\"/></svg>"
    },
    "within": {
      "@id": "https://iiif.lib.harvard.edu/manifests/drs:5981093",
      "@type": "sc:Manifest"
    }
  }}'
}

function a-add-sample-web-annotation2 {
	a-add-web-annotation '{
  "@context": "http://iiif.io/api/presentation/2/context.json",
  "@type": "oa:Annotation",
  "motivation": [
    "oa:tagging",
    "oa:commenting"
  ],
  "resource": [
    {
      "@type": "oa:Tag",
      "chars": "Tag"
    },
    {
      "@type": "dctypes:Text",
      "format": "text/html",
      "chars": "<p>Test 1c &amp; d</p>"
    }
  ],
  "on": {
    "@type": "oa:SpecificResource",
    "full": "https://iiif.lib.harvard.edu/manifests/drs:5981093/canvas/canvas-5981120.json",
    "selector": {
      "@type": "oa:SvgSelector",
      "value": "<svg xmlns=\"http://www.w3.org/2000/svg\"><path xmlns=\"http://www.w3.org/2000/svg\" d=\"M2839.67004,1958.16283l603.4384,0l0,0l603.4384,0l0,372.12034l0,372.12034l-603.4384,0l-603.4384,0l0,-372.12034z\" data-paper-data=\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\" id=\"rectangle_d8e37e58-64d5-47b5-934a-760f5f05129c\" fill-opacity=\"0\" fill=\"#00bfff\" fill-rule=\"nonzero\" stroke=\"#00bfff\" stroke-width=\"6.70487\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"10\" stroke-dasharray=\"\" stroke-dashoffset=\"0\" font-family=\"sans-serif\" font-weight=\"normal\" font-size=\"12\" text-anchor=\"start\" style=\"mix-blend-mode: normal\"/></svg>"
    },
    "within": {
      "@id": "https://iiif.lib.harvard.edu/manifests/drs:5981093",
      "@type": "sc:Manifest"
    }
  }}'
}

function a-add-sample-web-annotation3 {
	a-add-web-annotation '{
  "@context": "http://iiif.io/api/presentation/2/context.json",
  "@type": "oa:Annotation",
  "motivation": [
    "oa:tagging",
    "oa:commenting"
  ],
  "resource": [
    {
      "@type": "oa:Tag",
      "chars": "Tag"
    },
    {
      "@type": "dctypes:Text",
      "format": "text/html",
      "chars": "<p>HELLO WORLD</p>"
    }
  ],
  "on": {
    "@type": "oa:SpecificResource",
    "full": "https://iiif.lib.harvard.edu/manifests/drs:5981093/canvas/canvas-007.json",
    "selector": {
      "@type": "oa:SvgSelector",
      "value": "<svg xmlns=\"http://www.w3.org/2000/svg\"><path xmlns=\"http://www.w3.org/2000/svg\" d=\"M2839.67004,1958.16283l603.4384,0l0,0l603.4384,0l0,372.12034l0,372.12034l-603.4384,0l-603.4384,0l0,-372.12034z\" data-paper-data=\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\" id=\"rectangle_d8e37e58-64d5-47b5-934a-760f5f05129c\" fill-opacity=\"0\" fill=\"#00bfff\" fill-rule=\"nonzero\" stroke=\"#00bfff\" stroke-width=\"6.70487\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"10\" stroke-dasharray=\"\" stroke-dashoffset=\"0\" font-family=\"sans-serif\" font-weight=\"normal\" font-size=\"12\" text-anchor=\"start\" style=\"mix-blend-mode: normal\"/></svg>"
    },
    "within": {
      "@id": "https://iiif.lib.harvard.edu/manifests/drs:5981093",
      "@type": "sc:Manifest"
    }
  }}'
}

function a-update-sample-web-annotation3 {
  a-update-web-annotation $1 '{
  "@context": "http://iiif.io/api/presentation/2/context.json",
  "@type": "oa:Annotation",
  "motivation": [
    "oa:tagging",
    "oa:commenting"
  ],
  "resource": [
    {
      "@type": "oa:Tag",
      "chars": "Guten Tag!"
    },
    {
      "@type": "dctypes:Text",
      "format": "text/html",
      "chars": "<p>Goodbye World!</p>"
    }
  ],
  "on": {
    "@type": "oa:SpecificResource",
    "full": "https://iiif.lib.harvard.edu/manifests/drs:5981093/canvas/canvas-007.json",
    "selector": {
      "@type": "oa:SvgSelector",
      "value": "<svg xmlns=\"http://www.w3.org/2000/svg\"><path xmlns=\"http://www.w3.org/2000/svg\" d=\"M2839.67004,1958.16283l603.4384,0l0,0l603.4384,0l0,372.12034l0,372.12034l-603.4384,0l-603.4384,0l0,-372.12034z\" data-paper-data=\"{&quot;defaultStrokeValue&quot;:1,&quot;editStrokeValue&quot;:5,&quot;currentStrokeValue&quot;:1,&quot;rotation&quot;:0,&quot;deleteIcon&quot;:null,&quot;rotationIcon&quot;:null,&quot;group&quot;:null,&quot;editable&quot;:true,&quot;annotation&quot;:null}\" id=\"rectangle_d8e37e58-64d5-47b5-934a-760f5f05129c\" fill-opacity=\"0\" fill=\"#00bfff\" fill-rule=\"nonzero\" stroke=\"#00bfff\" stroke-width=\"6.70487\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"10\" stroke-dasharray=\"\" stroke-dashoffset=\"0\" font-family=\"sans-serif\" font-weight=\"normal\" font-size=\"12\" text-anchor=\"start\" style=\"mix-blend-mode: normal\"/></svg>"
    },
    "within": {
      "@id": "https://iiif.lib.harvard.edu/manifests/drs:5981093",
      "@type": "sc:Manifest"
    }
  }}'
}

function a-search-web-annotations-with-uri {
	uri=$1
	curl ${be}/webannotations/search?uri=${uri} | jq "."
}


a-use-localhost
