grammar AQL2;

root : FUNCTION '(' ( parameters ) ')';


FUNCTION: 'hello'
        | 'bye'
        ;

parameters : parameter ( ',' parameter )*;

parameter : STRING_PARAMETER | LONG_PARAMETER ;

STRING_PARAMETER : '"' ('\\"'|.)*? '"' ;

LONG_PARAMETER: [0-9]+ ;

WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
