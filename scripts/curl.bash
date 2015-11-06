# this is a collection of curl requests meant for copy-pasting into the shell, not for running as a whole
exit -1

source scripts/alexandria-functions.sh

# if you get nullpointerexception in the vf code, it's likely caused by eclipse not generating the code: run mvn compile on commandline
be=http://localhost:2015
be=https://alexandria.huygens.knaw.nl/
be=http://test.alexandria.huygens.knaw.nl/

uuid1="11111111-1111-1111-1111-111111111111"
uuid2="22222222-2222-2222-2222-222222222222"

curl -i -X PUT $be/resources/c28626d4-493a-4204-83d9-e9ae17e15654 -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "id":"c28626d4-493a-4204-83d9-e9ae17e15654",
  "ref":"whatever",
  "provenance":{
    "who":"someone",
    "when":"2015-06-11T08:03:47.868Z",
    "why":"because"
  }
}}'

ri=d28626d4-493a-4204-83d9-e9ae17e15654

curl -i -X PUT $be/resources/$ri -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "id":"d28626d4-493a-4204-83d9-e9ae17e15654",
  "ref":"whatEver"
}}'

curl -i -X POST $be/resources/$ri/subresources -H 'Content-type: application/json' \
--data-binary '{"subresource":{
  "sub":"title"
}}'
sri=

curl -i $be/resources/$ri/subresources

# confirm subresource
curl -i -X PUT $be/resources/$sri/state -H 'Content-type: application/json' \
--data-binary '{"state":"CONFIRMED"}'


curl -i -X PUT $be/resources/$ri -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "ref":"title"
}}'

curl -i -X POST $be/resources/$ri/annotations -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "value":"Bookmark",
  "provenance":{
    "who":"user",
    "when":"2015-06-19T15:03:47.868Z",
    "why":"interesting"
  }
}}'

curl -i -X POST $be/resources/$ri/annotations -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "type":"Genre",
  "value":"Comedy"
}}'

curl -i $be/resources/$ri/annotations

ai=
curl -i $be/annotations/$ai

# confirm annotation

curl -i -X POST $be/annotations/$ai/annotations -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "value":"Sureley you jest!",
  "type":"Comment",
  "provenance":{
    "who":"SomeoneWhoKnows",
    "when":"2015-06-22T13:00:00.000Z",
    "why":"I was not amused"
  }
}}'

curl -i $be/resources -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "ref":"reference",
  "provenance":{
    "who":"someone",
    "when":"2015-07-01T08:03:47.868Z",
    "why":"daarom"
  }
}}'

# tentative resource
curl -i $be/resources -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "ref":"Constantijn Huygens Biografie",
  "provenance":{
    "who":"someone",
    "when":"2015-06-22T08:03:47.868Z",
    "why":"daarom"
  }
}}'

curl -i $be/annotations/$ai
# deprecate confirmed annotation
curl -i -X PUT $be/annotations/$ai -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "value":"Totally new value",
  "provenance":{
    "who":"SomeoneElse",
    "when":"2015-06-22T13:00:00.000Z",
    "why":"test"
  }
}}'
nai=

curl -i $be/annotations/$nai
#confirm
curl -i -X PUT $be/annotations/$nai/state -H 'Content-type: application/json' \
--data-binary '{"state":"CONFIRMED"}'

# searches
curl -i -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
	"find" : "annotation",
	"where" : "type:eq(\"Tag\") who:eq(\"nederlab\") state:eq(\"CONFIRMED\")",
	"sort" : "when",
	"return" : "id,when,value,resource.id,resource.url,subresource.id,subresource.url"
}}'

curl -i -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
	"find" : "annotation",
	"where" : "type:eq(\"Tag\") who:eq(\"nederlab\") state:eq(\"CONFIRMED\") resource.id:eq(\"d28626d4-493a-4204-83d9-e9ae17e15654\")",
	"sort" : "when",
	"return" : "id,when,value,resource.id,resource.url,subresource.id,subresource.url"
}}'


curl -i -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
	"find" : "annotation",
	"sort" : "when",
	"return" : "id,when,value,resource.id,resource.url,subresource.id,subresource.url"
}}'

si=

curl -i $be/search/$si


### don't duplicate subresources

uuid1="11111111-1111-1111-1111-111111111111"

curl -i -X PUT $be/resources/$uuid1 -H 'Content-type: application/json' \
--data-binary '{"resource":{ "ref":"http://ref.ref/ref" }}'

curl -i -X POST $be/resources/$uuid1/subresources -H 'Content-type: application/json' \
--data-binary '{"subresource":{
  "sub":"/some/xpath"
}}'

curl -i -X POST $be/resources/$uuid1/subresources -H 'Content-type: application/json' \
--data-binary '{"subresource":{
  "sub":"/some/xpath"
}}'

curl -i $be/resources/$uuid1

###


### create dummy resources with annotations

for n in {0..9}; do 
	id="$n$n$n$n$n$n$n$n-$n$n$n$n-$n$n$n$n-$n$n$n$n-$n$n$n$n$n$n$n$n$n$n$n$n"
	curl -i -X PUT $be/resources/$id -H 'Content-type: application/json' \
	--data-binary "{\"resource\":{
	  \"id\":\"$id\",
	  \"ref\":\"reference $n\"
	}}"
	url=$(a-annotate-resource "$id" "Tag" "Test" | a-location)
	a-confirm $url
done

for n in {1..100}; do 
  a-generate-random-resource-with-annotation
done


curl -i -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
	"find" : "annotation",
	"where" : "who:eq(\"nederlab\")",
	"return" : "id,resource.url,subresource.url,type,value"
}}'

curl -i -H "${authheader}" -X PUT $be/admin -H 'Content-type: application/json' \
--data-binary '{
  "key" : "adminkey",
  "command" : "dump",
  "parameters" : {
    "format" : "kryo",
    "filename" : "alexandria-dump.kryo"
  }
}}'

curl -i -X PUT $be/admin -H 'Content-type: application/json' \
--data-binary '{
  "key" : "adminkey",
  "command" : "dump",
  "parameters" : {
    "format" : "graphml",
    "filename" : "alexandria-dump.xml"
  }
}}'

# generate error
curl -i -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
  "find" : "annotation",
  "where" : "state:inSet(\"CONFIRMED\",\"UNCONFIRMED\")",
  "return" : "id,resource.url,subresource.url,type,value"
}}'

<<<<<<< 391007ba779b5d21c96b29bea242116424d5b38c
# upload text, xml tags will be transformed into annotations
curl -i -X PUT $be/resources/$ri/text -H 'Content-type: application/json' \
--data-binary '{"text":"<xml>some text</xml>"}'


curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
  "find" : "annotation",
  "where" : "state:eq(\"CONFIRMED\")",
  "sort" : "when",
  "return" : "who"
}}'

curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
  "find" : "annotation",
  "where" : "state:eq(\"CONFIRMED\")",
  "sort" : "when",
  "distinct" : true,
  "return" : "who"
}}'
=======


# upload text, xml tags will be transformed into annotations
curl -i -X PUT $be/resources/$ri/text -H 'Content-type: application/json' \
--data-binary '{"text":"<xml>some text</xml>"}'
>>>>>>> [NLA-132] example call for the resource text endpoint
