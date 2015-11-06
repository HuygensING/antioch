#!/bin/bash
# alexandria-ctl

function usage {
  echo $"Usage: $0 export <test|acceptance|production> {base_url} {admin_key}"
  echo $"          import <test|acceptance|production> {base_url} {admin_key} {importfile}"
  echo $"                 ( *.gz importfiles are allowed)"
  exit 2
}

function a-export {
  instance=$1
  base_url=$2
  key=$3

  if [[ $# -ne 3 ]]; then
    usage
  fi

  curl ${base_url}/admin --include --silent --show-error --fail --request PUT --header 'Content-type: application/json' \
  --data-binary "{
    \"key\" : \"${key}\",
    \"command\" : \"export\",
    \"parameters\" : {
      \"format\" : \"gryo\",
      \"filename\" : \"alexandria-dump.kryo\"
    }
  }}" && \
  (
    timestamp=$(date +"%Y%m%d%H%M%S")
    versioned_dump=~/storage/${instance}/alexandria-dump${timestamp}.kryo
    mv ~/storage/${instance}/alexandria-dump.kryo ${versioned_dump} && \
    gzip ${versioned_dump}
  )
  echo  
}

function a-import {
  instance=$1
  base_url=$2
  key=$3
  importfile=$4

  if [[ $# -ne 4 ]]; then
    usage
  fi

  if [[ ! -e ${importfile} ]]; then
    echo "importfile ${importfile} not found"
    exit 2
  fi

  if [[ ${importfile} ==  *.gz ]]; then
    bak=/tmp/$(basename ${importfile})
    cp ${importfile} ${bak}
    gunzip ${bak}
    importfile=${bak/.gz//}
  fi

  curl -i -X PUT ${base_url}/admin -H 'Content-type: application/json' \
  --data-binary "{
    \"key\" : \"${key}\",
    \"command\" : \"import\",
    \"parameters\" : {
      \"format\" : \"gryo\",
      \"filename\" : \"${importfile}\"
    }
  }}"
}

case "$1" in
  export)
    a-export $2 $3 $4
    ;;
  import)
    a-import $2 $3 $4 $5
    ;;
  *)
    usage
esac

exit $?

# alexandria-ctrl.sh export test http://test.alexandria.huygens.knaw.nl "@NimdaCle#"
