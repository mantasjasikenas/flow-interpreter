grammar Flow;

program : statement+ EOF ;

statement
    : declaration
    | variableAssignment
    | assignment SEMICOLON
    | loopStatement
    | ifStatement
    | printStatement SEMICOLON
    ;

assignment : ID '=' expression ;

expression
    : INT                               #intExpression
    | DOUBLE                            #doubleExpression
    | STRING                            #stringExpression
    | CHAR                              #charExpression
    | BOOLEAN                           #booleanExpression
    | ID                                #idExpression
    | INT RANGE INT                     #rangeExpression
    | '(' expression ')'                #parenthesesExpression
    | expression intMultiOp expression  #intMultiOpExpression
    | expression intAddOp expression    #intAddOpExpression
    | expression relationOp expression  #relationOpExpression
    ;


loopStatement
    : forStatement
    | whileStatement
    ;

forStatement
    : FOR LPAREN (ID)
      IN expression RPAREN (controlStructureBody | SEMICOLON)
    ;

whileStatement
    : WHILE LPAREN expression RPAREN (controlStructureBody | SEMICOLON)
    ;


ifStatement : IF LPAREN expression relationOp expression RPAREN controlStructureBody
    (ELSE controlStructureBody)? ;

controlStructureBody: LBRACE statement+ RBRACE ;

printStatement : (PRINT | PRINTLN) LPAREN expression RPAREN ;

declaration
    : classDeclaration
    | methodDeclaration
    | arrayDeclaration
    | variableDeclaration
    ;

classDeclaration : CLASS ID LBRACE (classMemberDeclarations) RBRACE ;

methodDeclaration : FUN ID LPAREN (methodParams) RPAREN (COLON METHOD_TYPE)? LBRACE (statement)* RBRACE ;

arrayDeclaration : VARIABLE ID (COLON ARRAY_TYPE)? ASSIGN (('arrayOf' LPAREN (expression (COMMA expression)*)? RPAREN) | (LBRACE (expression (COMMA expression)*)? RBRACE)) SEMICOLON ;

variableDeclaration : VARIABLE ID (COLON TYPE)? (ASSIGN expression)? SEMICOLON ;

variableAssignment : ID ASSIGN expression SEMICOLON ;

classMemberDeclarations
    : (classMemberDeclaration SEMICOLON?)*
    ;

classMemberDeclaration
    : declaration
    ;

methodParams : (ID COLON (TYPE | ARRAY_TYPE) (COMMA ID COLON (TYPE | ARRAY_TYPE))*)? ;



INT     : [0-9]+ ;
DOUBLE  : [0-9]+ '.' [0-9]+ ;
STRING : QUOTE .*? QUOTE ;
CHAR    : '\'' .*? '\'' ;
BOOLEAN : 'true' | 'false' ;


TYPE    : 'Int' | 'Double' | 'String' | 'Char' | 'Boolean';
ARRAY_TYPE : 'Array' '<' TYPE '>';
METHOD_TYPE : TYPE | 'Unit' ;

relationOp : '==' | '!=' ;
intMultiOp : '*' | '/' | '%' ;
intAddOp : '+' | '-' ;

ASSIGN: '=' ;
SEMICOLON: ';' ;
APOSTROPHE: '\'' ;
QUOTE: '"' ;
COLON : ':' ;
LPAREN: '(' ;
RPAREN: ')' ;
LBRACE: '{' ;
RBRACE: '}' ;
RANGE: '..';
COMMA: ',';

PRINT   : 'print';
PRINTLN : 'println';
IF: 'if' ;
ELSE: 'else' ;
FOR: 'for' ;
WHILE: 'while' ;
IN: 'in' ;
CLASS: 'class' ;
FUN: 'fun' ;
VARIABLE: ('var' | 'val') ;
RETURN: 'return' ;

ID: [a-zA-Z]+ [a-zA-Z0-9]* ;
COMMENT : ( ('//' | '#') ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WHITESPACE      : [ \t\r\n]+ -> skip ;