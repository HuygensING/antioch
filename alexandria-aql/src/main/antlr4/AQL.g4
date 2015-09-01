grammar AQL;

@header {
package nl.knaw.huygens.alexandria.antlr;
}

root : subquery ( ',' subquery )*;

subquery : FIELDNAME ':' function '(' parameters ')';

parameters : PARAMETER ( ',' PARAMETER )*;

FIELDNAME: [a-z.]+ ;

function : 'eq' | 'match' | 'inSet' | 'inRange';

PARAMETER: [a-zA-Z0-9"]+ ;

WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
