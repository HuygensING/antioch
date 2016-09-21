grammar AQL2;

root : FUNCTION '(' ( parameters ) ')' | FUNCTION '()';


FUNCTION: 'hello'
        | 'bye'
        ;

parameters : parameter ( ',' parameter )*;

parameter : STRING_PARAMETER | LONG_PARAMETER | BOOLEAN_PARAMETER;

STRING_PARAMETER : '"' ('\\"'|.)*? '"' ;

LONG_PARAMETER: [0-9]+ ;

BOOLEAN_PARAMETER: 'true' | 'false' ;

WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
