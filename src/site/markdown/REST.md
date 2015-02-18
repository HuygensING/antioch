RESTful Alexandria
==================

Base url: `https://alexandria.huygens.knaw.nl`

Create a resource
-----------------
```
/resources (POST)
Authorization: auth-token-1234                      # Some token identifying 'nederlab'

{
    "resource": {
        "id": "some-uuid-1",
        "ref": "http://nederlab.nl/titles/some-uuid-1",
        "createdBy": "root",                        # root  when unspecified, may be overriden (unchecked)
        "createdOn": "2015-02-17 07:48:50+01:00"    # now() when unspecified, may be overridden (RFC-3339)
    }
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
    "resource": {
        "id": "some-uuid-1",
        "ref": "http://meertens.knaw.nl/titles/some-uuid-1",
        "annotations": [],
        "createdBy": "root",
        "createdOn": "2015-02-17 07:48:50+01:00"
    }
}
```

Add an annotation
-----------------
```
/resources/some-uuid-1/annotations (POST)
Authorization: auth-token-1234

{
    "annotation": {
        "type": "emotion",
        "value": "happy",
        "createdBy": "nederlab-user-1"
    }
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
    "annotation": {
        "type": "emotion",
        "value": "happy",
        "annotations": [],
        "createdBy": "nederlab-user-1",
        "createdOn": "2015-02-17 07:56:04+01:00"
    }
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

{
    "annotations": [
        {
            "type": "emotion",
            "value": "happy",
            "annotations": [],
            "createdBy": "nederlab-user-1",
            "createdOn": "2015-02-17 07:48:50+01:00"
        }
    ]
}
```

Add another annotation
----------------------
```
/resouces/some-uuid-1/annotations (POST)
Authorization: auth-token-1234

{
    "annotation": {
        "type": "emotion",
        "value": "cheerful"
    }
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
		    "type": "emotion",
		    "value": "happy",
			"annotations": [],
			"createdBy": "nederlab-user-1",
			"createdOn": "2015-02-17 07:48:50+01:00"
		},
		{
		    "type": "emotion",
		    "value": "cheerful",
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
            "type": "emotion",
            "value": "happy",
            "annotations": [],
            "createdBy": "nederlab-user-1",
            "createdOn": "2015-02-17 07:48:50+01:00"
        },
        {
            "type": "emotion",
            "value": "cheerful",
            "annotations": [],
            "createdBy": "root",
            "createdOn": "2015-02-17 08:11:24+01:00"
        }
    ],
    "createdBy": "nederlab-user-1",
    "createdOn": "2015-02-17 07:48:50+01:00",
    "updatedOn": "2015-02-17 08:11:24+01:00"
}
```
