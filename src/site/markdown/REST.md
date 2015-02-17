Example REST calls:
===================

Base url: `https://alexandria.huygens.knaw.nl`

Create a resource
-----------------
```html
/resources (POST)
Authorization: auth-token-1234

{
    "id": "some-uuid-1",
    "ref": "http://meertens.knaw.nl/titles/some-uuid-1",
    "createdBy": "root"                         # Who decides?
}
```

```
=> 201 (Created)
Location: https://alexandria.huygens.knaw.nl/resources/some-uuid-1
```

Get a resource
--------------
```html
/resources/some-uuid-1 (GET)
Authorization: auth-token-1234
```

```
=> 200 (Ok)

{
    "id": "some-uuid-1",
    "ref": "http://meertens.knaw.nl/titles/some-uuid-1",
    "annotations": [],
    "createdBy": "root",
    "createdOn": "2015-02-17 07:48:50+01:00"      # RFC-3339 in {date,seconds,ns}
}
```

Add an annotation
-----------------
```
/resources/some-uuid-1/annotations (POST)
Authorization: auth-token-1234

{
    "emotion": "happy",
    "createdBy": "root"

}
```

```
=> 201 (Created)
Location: https://alexandria.huygens.knaw.nl/annotations/anno-id-1
```

Get a specific annotation
-------------------------
```
/annotations/anno-id-1 (GET)
Authorization: auth-token-1234
```

```
=> 200 (Ok)

{
    "emotion": "happy",
    "annotations": [],
    "createdBy": "root",
    "createdOn": "2015-02-17 07:56:04+01:00"
}
```

Get annotations on specific resource
------------------------------------
```
/resources/some-uuid-1/annotations (GET)
Authorization: auth-token-1234
```

```
=> 200 (Ok)

[
    {
        "annotations": [],
        "createdBy": "root",
        "createdOn": "2015-02-17 07:48:50+01:00",
        "emotion": "happy"
    }
]
```

Add another annotation
----------------------
```
/resouces/some-uuid-1/annotations (POST)
Authorization: auth-token-1234

{
    "emotion": "cheerful"
}
```

```
=> 201 (Created)
Location: https://alexandria.huygens.knaw.nl/annotations/anno-id-2
```

Get annotations on `some-uuid-1` again
------------------------------------
```
/resources/some-uuid-1/annotations (GET)
Authorization: auth-token-1234
```

```
=> 200 (Ok)

{
	"annotations": [
		{
			"emotion": "happy",
			"annotations": [],
			"createdBy": "root",
			"createdOn": "2015-02-17 07:48:50+01:00"
		},
		{
			"emotion": "cheerful",
			"annotations": [],
			"createdBy": "root",
			"createdOn": "2015-02-17 08:11:24+01:00"
		}
	]
}
```

Get resource `some-uuid-1` again
--------------------------------
```
/resources/some-uuid-1 (GET)
Authorization: auth-token-1234
```

```
=> 200 (Ok)

{
    "id": "some-uuid-1",
    "ref": "http://meertens.knaw.nl/titles/some-uuid-1",
    "annotations": [
        {
            "emotion": "happy",
            "annotations": [],
            "createdBy": "root",
            "createdOn": "2015-02-17 07:48:50+01:00"
        },
        {
            "emotion": "cheerful",
            "annotations": [],
            "createdBy": "root",
            "createdOn": "2015-02-17 08:11:24+01:00"
        }
    ],
    "createdBy": "root",
    "createdOn": "2015-02-17 07:48:50+01:00",
    "updatedOn": "2015-02-17 08:11:24+01:00"
}
```
