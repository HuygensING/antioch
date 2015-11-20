grammar AQL2;

query: findClause whereClause? sortClause? returnClause?;

findClause: 'find' ( 'annotation' | 'resource' );  

whereClause : 'where' subquery ( 'and' subquery )*;

subquery : FIELDNAME ':' functionCall;

functionCall : eqFunction
             | matchFunction
             | insetFunction
             | inrangeFunction
             ;

eqFunction : 'eq(' functionParameter ')';

matchFunction : 'match(' functionParameter ')';

insetFunction : 'inSet(' insetParameters ')';

insetParameters : STRINGPARAMETER ( ',' STRINGPARAMETER )*
                | LONGPARAMETER ( ',' LONGPARAMETER )*
                ;

inrangeFunction : 'inRange(' inrangeParameters ')';

inrangeParameters : STRINGPARAMETER ',' STRINGPARAMETER
                  | LONGPARAMETER ',' LONGPARAMETER
                  ;

functionParameter : STRINGPARAMETER | LONGPARAMETER ;

sortClause : 'sort on' sortParameter (',' sortParameter)*;

sortParameter : ( ASC_PREFIX | DESC_PREFIX )? FIELDNAME;

returnClause : 'return' ( fieldnames | distinctFieldnames );

fieldnames : FIELDNAME ( ',' FIELDNAME)*;

distinctFieldnames : 'distinct(' fieldnames ')';

FIELDNAME : 'id' 
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

STRINGPARAMETER : '"' ('\\"'|.)*? '"' ;

LONGPARAMETER: [0-9]+ ;

ASC_PREFIX: '+';
DESC_PREFIX: '-';

WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
