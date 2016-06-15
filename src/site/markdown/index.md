# Alexandria

Primarily developed in the context of the [Nederlab project](https://www.nederlab.nl/), Alexandria 
is a *graph-based text and annotation repository*. While in its primary context, it serves as a store
for annotations targeted at *linguistic corpora*, Alexandria's conceptual design of texts, annotations
and their relationships has the potential to be applied in other domains and use cases as well,
for instance in the field of *literary studies*. This project aims at leveraging Alexandria's
potential value as a generic storage and query facility for annotated texts by

1. *abstracting* from Nederlab-specific requirements of linguistic annotation,
1. *building* a repository software with state-of-the-art, web-based technologies, and
1. *connecting* Alexandria to similar initiatives by providing a rich set of data interchange options.

## Features

Alexandria can

* model texts and annotations on the same level, allowing for the annotation of texts and
  annotations,
* *store, retrieve and query* texts/annotations (aka. _resources_),
* distinguish *versions* of those resources, 
* provide information about the *provenance* of its stored resources,
* annotate resources within the same repository as well as *remote resources*, and
* structure resources hierarchically or as networked entities. 

## Software Architecture

Being a web-based solution, Alexandria 

* is implemented as a *RESTful service*, exposing its functions via HTTP endpoints/resources 
  on the network,
* supports several *graph database backends* for the storage of its data, from small, embedded
  databases to clustered setups for larger datasets,
* offers various export formats, and
* has its own, domain-specific query language.

## Download

...

## Texts as Graphs: Research/ Bibliography

Alexandria is part of a larger community interested in the annotation of textual resources. As such
it draws inspiration from many sources, some of which are:

* W3C Web Annotation Working Group. [Homepage](https://www.w3.org/annotation/)

* Andrews, Tara Lee; Macé, Caroline (2013). Beyond the tree of texts: Building an empirical model of 
  scribal variation through graph analysis of texts and stemmata. Literary and Linguistic
  Computing, 28(4), pp. 504-521. Oxford University Press.
  [10.1093/llc/fqt032](http://dx.doi.org/10.1093/llc/fqt032)
  
* Schmidt, D. and Colomb, R. ‘A data structure for representing multi-version texts online’. 
  International Journal of Human-Computer Studies. 67.6: 2009, 497-514. 
  [doi:10.1016/j.ijhcs.2009.02.001](http://dx.doi.org/10.1016/j.ijhcs.2009.02.001).
  
* C. M. Sperberg-McQueen 1 and Claus Huitfeldt: GODDAG: A Data Structure for Overlapping Hierarchies.
  DDEP-PODDP 2000, ed. P. King and E.V. Munson, Lecture Notes in Computer Science 2023 (Berlin: Springer, 2004). 
  pp. 139-160.
  [Online preprint](http://cmsmcq.com/2000/poddp2000.html)

* Nancy Ide, Keith Suderman. GrAF: A Graph-based Format for Linguistic Annotations.
  [Online](http://aclweb.org/anthology/W/W07/W07-1501.pdf).
