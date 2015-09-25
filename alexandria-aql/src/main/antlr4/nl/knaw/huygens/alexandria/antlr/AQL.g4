grammar AQL;

root : subquery ( subquery )*;

subquery : FIELDNAME ':' FUNCTION '(' parameters ')';

FIELDNAME: 'id' 
         | 'url' 
         | 'type' 
         | 'value'
         | 'resource.id' 
         | 'subresource.id'
         | 'resource.url' 
         | 'subresource.url'
         | 'state'
         | 'who' 
         | 'when' 
         | 'why'
         ;

FUNCTION : 'eq'
         | 'match'
         | 'inSet' 
         | 'inRange'
         ;

parameters : parameter ( ',' parameter )*;

parameter : STRINGPARAMETER | LONGPARAMETER ;

STRINGPARAMETER : '"' [a-zA-Z 0-9.;:\-\*\?\!]+ '"' ;

LONGPARAMETER: [0-9]+ ;

WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
