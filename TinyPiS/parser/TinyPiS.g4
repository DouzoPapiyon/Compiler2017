// antlr4 -package parser -o antlr-generated  -no-listener parser/TinyPiS.g4
grammar TinyPiS;

prog: varDecls stmt
	;
	
varDecls: ('var' IDENTIFIER ';')*
	;

stmt: '{' stmt* '}'	# compoundStmt
	| IDENTIFIER '=' expr ';'	# assignStmt
	| 'if' '(' expr ')' stmt 'else' stmt    # ifStmt
	| 'while' '(' expr ')' stmt	# whileStmt
	| 'print' expr ';'  # printStmt
	;

expr: orExpr
      ;
      
orExpr: orExpr OROP andExpr
	| andExpr
	;
	
andExpr: andExpr ANDOP addExpr
	| addExpr
	;

addExpr: addExpr (ADDOP|SUBOP) mulExpr
	| mulExpr
	;

mulExpr: mulExpr MULOP unaryExpr
	| unaryExpr
	;

unaryExpr: (NOTOP|SUBOP)	unaryExpr #notExpr
	| VALUE			# literalExpr
	| IDENTIFIER			# varExpr
	| '(' expr ')'			# parenExpr
	;

ADDOP: '+';
SUBOP: '-';
MULOP: '*'|'/';
ANDOP: '&';
OROP : '|';
NOTOP: '~';

IDENTIFIER: [_a-zA-Z][_a-zA-Z0-9]*;
VALUE: [0]|[1-9][0-9]*;
WS: [ \t\r\n] -> skip;