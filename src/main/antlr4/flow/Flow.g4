grammar Flow;

program : globalStatement+ EOF ;

globalStatement
    : classDeclaration
    | methodDeclaration
    | statement;

statement
    : methodInvocation SEMICOLON
    | classObjectVariableSetter
    | classObjectVariableGetter
    | declaration
    | variableAssignment
    | loopStatement
    | ifStatement
    | printStatement
    | iOStatement
    ;



expression
    : INT                               #intExpression
    |DOUBLE                            #doubleExpression
    | STRING                            #stringExpression
    | CHAR                              #charExpression
    | BOOLEAN                           #booleanExpression
    | ID                                #idExpression
    | classObjectVariableGetter         #classObjectVariableGetterExpression
    | methodInvocation                  #methodInvocationExpression
    | LPAREN expression RPAREN          #parenthesesExpression
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

iOStatement
    : readStatement
     | readLineStatement
     | readFileStatement
     | writeFileStatement
     | readStatement; // TODO move print statement to IO statement

readStatement : READ LPAREN RPAREN SEMICOLON ;

readLineStatement : READ_LINE LPAREN RPAREN SEMICOLON;

readFileStatement : READ_FILE LPAREN (STRING | ID) RPAREN SEMICOLON ;

writeFileStatement : WRITE_FILE LPAREN (STRING | ID) COMMA (STRING | ID) RPAREN SEMICOLON ;

methodBodyStatement : statement | returnStatement ;

methodStructureBody : LBRACE methodBodyStatement* RBRACE ;

controlStructureBody: LBRACE statement* RBRACE ;

returnStatement : RETURN expression SEMICOLON ;

printStatement : (PRINT | PRINTLN) LPAREN expression? RPAREN SEMICOLON ;

declaration
    : arrayDeclaration
    | objectDeclaration
    | variableDeclaration
    ;

classDeclaration : CLASS ID LBRACE (classMember)* RBRACE ;

classConstructor : CONSTRUCTOR LPAREN (methodParams)? RPAREN controlStructureBody ;

methodDeclaration : FUN ID LPAREN (methodParams)? RPAREN (COLON (TYPE | UNIT))? methodStructureBody ;

arrayDeclaration : VARIABLE ID (COLON ARRAY_TYPE)? ASSIGN (('arrayOf' LPAREN (expression (COMMA expression)*)? RPAREN) | (LBRACE (expression (COMMA expression)*)? RBRACE)) SEMICOLON ;

objectDeclaration : VARIABLE ID ASSIGN (NEW ID LPAREN (methodArgs)? RPAREN) SEMICOLON ;

variableDeclaration : VARIABLE ID (COLON TYPE)? (ASSIGN expression)? SEMICOLON ;

variableAssignment : ID ASSIGN expression SEMICOLON ;

classMember
    : classConstructor
    | methodDeclaration
    | declaration
    ;

classObjectVariableGetter : ID DOT ID ;

classObjectVariableSetter : ID DOT ID ASSIGN expression SEMICOLON ;

methodInvocation : (ID DOT)? ID LPAREN (methodArgs)? RPAREN ;

methodParams : (ID COLON (TYPE | ARRAY_TYPE) (COMMA ID COLON (TYPE | ARRAY_TYPE))*) ;

methodArgs : (expression (COMMA expression)*) ;



INT     : [0-9]+ ;
DOUBLE  : [0-9]+ '.' [0-9]+ ;
STRING  : QUOTE .*? QUOTE ;
CHAR : APOSTROPHE .? APOSTROPHE ;
BOOLEAN : 'true' | 'false' ;

TYPE    : 'Int' | 'Double' | 'String' | 'Char' | 'Boolean';
ARRAY_TYPE : 'Array' '<' TYPE '>';


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
READ: 'read';
READ_LINE   : 'readLine'; // TODO add read line from console

READ_FILE    : 'readFile'; // TODO add read file
WRITE_FILE   : 'writeFile'; // TODO add write file

UNIT: 'Unit' ;
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