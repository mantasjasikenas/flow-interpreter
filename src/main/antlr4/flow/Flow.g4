grammar Flow;

program : statement+ EOF ;

statement
    : classDeclaration
    | methodInvocation SEMICOLON
    | declaration // global declaration
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
    | NEW ID                            #newExpression
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
      IN rangeExpression RPAREN controlStructureBody
    ;

rangeExpression
    : expression RANGE expression
    ;

whileStatement
    : WHILE LPAREN expression RPAREN controlStructureBody
    ;


ifStatement : IF LPAREN expression relationOp expression RPAREN controlStructureBody
    (ELSE controlStructureBody)? ;

controlStructureBody: LBRACE statement* RBRACE ;

printStatement : (PRINT | PRINTLN) LPAREN expression RPAREN ;

declaration
    : methodDeclaration
    | arrayDeclaration
    | variableDeclaration
    ;

classDeclaration : CLASS ID LBRACE (classMember)* RBRACE ;

classConstructor : CONSTRUCTOR LPAREN (methodParams)? RPAREN controlStructureBody ;

methodDeclaration : FUN ID LPAREN (methodParams)? RPAREN (COLON METHOD_TYPE)? LBRACE (statement)* RBRACE ;

arrayDeclaration : VARIABLE ID (COLON ARRAY_TYPE)? ASSIGN (('arrayOf' LPAREN (expression (COMMA expression)*)? RPAREN) | (LBRACE (expression (COMMA expression)*)? RBRACE)) SEMICOLON ;

variableDeclaration : VARIABLE ID (COLON TYPE)? (ASSIGN expression)? SEMICOLON ;

variableAssignment : ID ASSIGN expression SEMICOLON ;

classMember
    : classConstructor
    | declaration
    ;


methodInvocation : ID DOT ID LPAREN (methodArgs)? RPAREN ;

methodParams : (ID COLON (TYPE | ARRAY_TYPE) (COMMA ID COLON (TYPE | ARRAY_TYPE))*) ;

methodArgs : (expression (COMMA expression)*) ;



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
DOT: '.';

PRINT   : 'print';
PRINTLN : 'println';
IF: 'if' ;
ELSE: 'else' ;
FOR: 'for' ;
WHILE: 'while' ;
IN: 'in' ;
NEW: 'new' ;
CLASS: 'class' ;
FUN: 'fun' ;
VARIABLE: ('var' | 'val') ;
RETURN: 'return' ;
CONSTRUCTOR: 'constructor' ;

ID: [a-zA-Z]+ [a-zA-Z0-9]* ;
COMMENT : ( ('//' | '#') ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WHITESPACE      : [ \t\r\n]+ -> skip ;