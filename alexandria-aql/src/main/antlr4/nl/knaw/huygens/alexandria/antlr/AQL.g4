grammar AQL;

root
  : subQuery ( subQuery )*
  ;

subQuery
  : FIELD_NAME ':' FUNCTION '(' parameters ')'
  ;

FIELD_NAME
  : 'id'
  | 'url' 
  | 'type' 
  | 'value'
  | 'resource.id' 
  | 'subresource.id'
  | 'resource.url' 
  | 'subresource.url'
  | 'resource.ref' 
  | 'subresource.sub'
  | 'state'
  | 'who' 
  | 'when' 
  | 'why'
  ;

FUNCTION
  : 'eq'
  | 'match'
  | 'inSet' 
  | 'inRange'
  ;

parameters
  : parameter ( ',' parameter )*
  ;

parameter
  : STRING_PARAMETER
  | LONG_PARAMETER
  ;

STRING_PARAMETER
  : '"' ('\\"'|.)*? '"'
  ;

LONG_PARAMETER
  : [0-9]+
  ;

WS
  : [ \t\r\n]+ -> skip // skip spaces, tabs, newlines
  ;
