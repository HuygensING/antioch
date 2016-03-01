# usage: source cwg-functions.sh
# shell methods for handling project CWG

function cwg-upload {
  a-log "create root resource for CWG project"
  ri=$(uuidgen)
  cwg-create-root-resource $ri
  root=$be/resources/$ri

  a-log "add baselayer definition to root resource"
  curl -i -H "${authheader}" -X PUT $root/baselayerdefinition -H 'Content-type: application/json' --data @"cwg-bld.json"
  curl $root/baselayerdefinition

  a-log "add Hugo Grotius resources"
  cwg-create-subresource $root "groo001: Hugo Grotius corpus"
  groo001=$suburi
  a-log "add Constantijn Huygens resources"
  cwg-create-subresource $root "huyg001: Constantijn Huygens corpus"
  huyg001=$suburi
  a-log "add Christiaan Huygens resources"
  cwg-create-subresource $root "huyg003: Christiaan Huygens corpus"
  huyg003=$suburi

  a-log "display root"
  curl $root

  a-log "add letter groo001-1539-01"
  cwg-add-letter $groo001 "groo001-1539-01"
  a-log "add letter huyg001-0035"
  cwg-add-letter $huyg001 "huyg001-0035"
  a-log "add letter huyg001-2976"
  cwg-add-letter $huyg001 "huyg001-2976"
  a-log "add letter huyg003-0052"
  cwg-add-letter $huyg003 "huyg003-0052"
  a-log "add letter huyg003-1376"
  cwg-add-letter $huyg003 "huyg003-1376"

  a-log "DONE"
}

function cwg-create-root-resource {
  # because we PUT the resource, it has status CONFIRMED
  id=$1
  curl -i -X PUT $be/resources/$id --header "${authheader}" --header 'Content-type: application/json' \
  --data-binary "{\"resource\":{
    \"id\":\"$id\",
    \"ref\":\"CWG-corpus\"
  }}"
}

# usage: cwg-create-subresource resource title
function cwg-create-subresource {
  suburi=$(curl -i -s --header "${authheader}" -X POST "$1/subresources" \
    --header 'Content-type: application/json' --data-binary "{\"subresource\":{ \"sub\":\"$2\" }}" 2>/dev/null | cwg-location)
  a-confirm ${suburi}
}

function cwg-set-text-from-file {
  a-log "Setting resource text for $1/text"
  curl -i --header "${authheader}" -X PUT $1/text --header 'Content-Type:application/octet-stream' --data @"$2"
}

# usage: cwg-add-letter corpus-resource letter-id
function cwg-add-letter {
  cwg-create-subresource $1 $2
  curl $suburi
  cwg-set-text-from-file $suburi "$2.xml"
  curl $suburi/text
}

function cwg-location {
  grep "Location:"|cut -d\  -f2|tr -d '\r'|sed -e "s/https:\/\/acc.alexandria.huygens.knaw.nl/http:\/\/tc24alex.huygens.knaw.nl\/alexandria/g"
}
