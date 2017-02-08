# this is a collection of curl requests meant for copy-pasting into the shell, not for running as a whole
exit -1

source scripts/alexandria-functions.sh

# if you get nullpointerexception in the vf code, it's likely caused by eclipse not generating the code: run mvn compile on commandline
be=http://localhost:2015
be=https://alexandria.huygens.knaw.nl/
be=http://test.alexandria.huygens.knaw.nl/

uuid1="11111111-1111-1111-1111-111111111111"
uuid2="22222222-2222-2222-2222-222222222222"

curl -i -X PUT ${be}/resources/c28626d4-493a-4204-83d9-e9ae17e15654 -H 'Content-type: application/json' \
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

curl -i -X PUT ${be}/resources/${ri} -H 'Content-type: application/json' \
--data-binary '{"resource":{
  "id":"d28626d4-493a-4204-83d9-e9ae17e15654",
  "ref":"whatEver"
}}'

curl -i -H "${authheader}" -X POST ${be}/resources/${ri}/subresources -H 'Content-type: application/json' \
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

curl -i -H "${authheader}" -X PUT ${be}/resources/599fad4f-21ca-4488-94f7-66fba8b39aba/text -H 'Content-type: text/xml' --data '<xml>hoera!</xml>'

curl -i -H "${authheader}" -H 'Content-type: application/json' -X PUT ${be}/resources/599fad4f-21ca-4488-94f7-66fba8b39aba/text  --data '{"text":{"body":"Dit is een tekst"}}}'

curl -i -H "${authheader}" -X POST $be/resources/$ri/annotations -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "locator" : "id:p0",
  "type":"lang",
  "value":"nl",
  "provenance":{
    "who":"cwg-",
    "why":"interesting"
  }
}}'
curl -i -H "${authheader}" -X POST $be/annotations/$ai/annotations -H 'Content-type: application/json' \
--data-binary '{"annotation":{
  "locator" : "id:p-12",
  "type":"cwg:a-lang",
  "value":"expecting badrequest",
  "provenance":{
    "who":"cwg-",
    "why":"interesting"
  }
}}'

## locator annotation limitationa:

#- on resource -> annotation with locator on annotation gives badrequest
#- on resource -> annotation with locator on resource without text gives badrequest
#- on resource -> annotation with invalid locator on resource with text gives badrequest
#- on resource -> annotation with locator on resource without text gives badrequest
#- on resource -> annotation with id locator on resource with text but ☺gives badrequest



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

curl -i -H "${authheader}" $be/resources/$ri/baselayerdefinition

a-dry-run "<body></body>"

a-dry-run '<body><p>Hello world!</p></body>'

a-dry-run '<body id="body1">
  <p id="p1">Hello world!</p>
</body>'

a-dry-run '<body id="body1" lang="en">
  <p id="p1" lang="en">Hello world!</p>
</body>'

a-dry-run '<body id="body1"><p id="p1"><hi rend="i">Hello <lb/> M<sup id="sup1">r</sup>. <persName>Jones</persName>!</hi></p></body>'

a-dry-run '<body xml:id="body1">
  <p xml:id="p1">Hello world!</p>
</body>'

a-dry-run '<body xml:id="body1" lang="en">
  <p xml:id="p1" lang="en">Hello world!</p>
</body>'

a-dry-run '<body xml:id="body1"><p xml:id="p1"><hi rend="i">Hello <lb/> M<sup xml:id="sup1">r</sup>. <persName>Jones</persName>!</hi></p></body>'

a-dry-run '<body xml:id="body1"><p xml:id="p1"><hi rend="i">Hello<note who="me">WTF?<note who="boss">language!</note></note> <lb/> M<sup xml:id="sup1">r</sup>. <persName>Jones</persName>!</hi></p></body>'



curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query" : {
      "find" : "annotation",
      "where" : "state:eq(\"CONFIRMED\") resource.id:inSet(\"73c04232-f5f4-4d69-88ea-5ba4a669b72d\",\"c88423cc-3c4d-472e-ac99-2c67eabf02dc\")",
      "sort" : "-when",
      "distinct" : true,
      "pageSize" : 100,
      "return" : "id,value,resource.id,subresource.id"
    }}'


// Meertens bug report
curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query" : {
"find" : "annotation",
"where" : "state:eq(\"CONFIRMED\") who:eq(\"gebruikertje_meertens.knaw.nl\") resource.id:inSet(\"e023002c-011b-11e4-b0ff-51bcbd7c379f\",\"e0a2bd62-011b-11e4-b0ff-51bcbd7c379f\")",
"sort" : "-when",
"distinct" : true,
"pageSize" : 100,
"return" : "id,value,resource.id,subresource.id"
}}'

a-dry-run-from-file scripts/cwg/huyg003-1376.xml

curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
  "find" : "annotation",
  "where" : "resource.id:inSet(\"b5b6e318-f74d-449c-af30-217140b95105\",\"e7abb6a6-5df2-4428-82ea-f2a7af31b892\",\"1785cda1-73bb-44e6-bc25-620ff3148e91\")",
  "return" : "id,resource.url"
}}'

curl -i -H "${authheader}" -X POST $be/searches -H 'Content-type: application/json' \
--data-binary '{"query":{
  "find" : "annotation",
  "where" : "resource.id:inSet(\"b5b6e318-f74d-449c-af30-217140b95105\",\"e7abb6a6-5df2-4428-82ea-f2a7af31b892\",\"1785cda1-73bb-44e6-bc25-620ff3148e91\")",
  "return" : "list(id),resource.url"
}}'


curl -i -H "${authheader}" -X PUT $be/resources/$ri/text/views/no-hi-l -H 'Content-type: application/json' \
--data-binary '{
    "textView": {
      "description" : "Do not show hi or l tags ",
      "ignoredElements": ["note"],
      "excludedElements": [ "hi", "l" ]
    }
  }'

curl -i -H "${authheader}" -X POST $be/commands/add-unique-id -H 'Content-type: application/json' \
--data-binary "{
      \"resourceIds\" : [\"$ri\"],
      \"elements\": [ \"div\", \"p\" ]
  }"

curl -i -H "${authheader}" -X POST $be/commands/wrap-content-in-element -H 'Content-type: application/json' \
--data-binary "{
      \"resourceIds\" : [\"$ri\"],
      \"xmlIds\": [ \"\", \"\" ]
      \"element\": {
        \"name\" : \"hi\",
        \"attributes\" : {
          \"rend\" : \"green\"
        }
      }
  }"


##

condition: "attribute(ref).equals(#ed)"
condition: "attribute(ref).firstOf('#ed','#ad1','')"

{
    "textView": {
      "description" : "Do not show hi or l tags ",
      "elements" : [
        ":default" : {
          "elementMode" : "show" # show <element> + children
          "attributeMode" : "show" # show <element> + children
        },
        "note" : {
          "mode" : "hideElement" # don't show <element> + children
        },
        "l" : {
          "mode" : "hideElementTag" # don't show <element> tag, show children
        },
        "hi" : {
          "mode" : "showElement"
          "condition" : "attribute(rend).equals('red')" # show according to mode if condition met, use default 
        }
      ]
      "ignoredElements": ["note"],
      "excludedElements": [ "hi", "l" ]
    }
  }

#  elementMode: (optional, use default settings if absent)
#    show : show <element> + children
#    hide : don't show <element> + children 
#    hideTag : don't show <element> tag, show children

#  attributeMode: (optional, use default settings if absent)
#    showAll : show all attributes
#    showOnly([attribute1,  ...]) : show only indicated attributes
#    hideAll : don't show any attribute
#    hideOnly([attribute1, ...]) : show all attributes except the indicated ones

# when (optional, always when 'when' not given)
#    attribute(rend).is('red')
#    attribute(resp).firstOf('#ed0','#ed1','')
