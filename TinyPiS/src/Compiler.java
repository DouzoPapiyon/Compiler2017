import java.io.IOException;
import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.TinyPiSLexer;
import parser.TinyPiSParser;

public class Compiler extends CompilerBase {
	//演習11追加
	void compileStmt(ASTNode ndx, Environment env) {
		if (ndx instanceof ASTCompoundStmtNode) {
			ASTCompoundStmtNode nd = (ASTCompoundStmtNode) ndx;
			ArrayList<ASTNode> stmts = nd.stmts;
			for (ASTNode child: stmts)
				compileStmt(child, env);
		} else if (ndx instanceof ASTAssignStmtNode) {
			ASTAssignStmtNode nd = (ASTAssignStmtNode) ndx;
			Variable var = env.lookup(nd.var);
			if (var == null)
				throw new Error("undefined variable: "+nd.var);
			compileExpr(nd.expr, env);
			if (var instanceof GlobalVariable) {
				GlobalVariable globalVar = (GlobalVariable) var;
				emitLDC(REG_R1, globalVar.getLabel());
				emitSTR(REG_DST, REG_R1, 0);
			} else
				throw new Error("Not a global variable: "+nd.var);
		} else if (ndx instanceof ASTIfStmtNode) {
			ASTIfStmtNode nd = (ASTIfStmtNode) ndx;
			String elseLabel = freshLabel();
			String endLabel = freshLabel();
			compileExpr(nd.cond, env);
			emitRI("cmp", REG_DST, 0);
			emitJMP("beq", elseLabel);
			compileStmt(nd.thenClause, env);
			emitJMP("b", endLabel);
			emitLabel(elseLabel);
			compileStmt(nd.elseClause, env);
			emitLabel(endLabel);
		} 
		//while文の追加
		else if (ndx instanceof ASTWhileStmtNode) {
			ASTWhileStmtNode nd = (ASTWhileStmtNode) ndx;
			String endLabel = freshLabel();
			compileExpr(nd.cond, env);
			emitRI("cmp", REG_DST, 0);
			emitJMP("bne", endLabel);
			compileStmt(nd.stmt, env);
			emitLabel(endLabel);
			
			//演習13追加
		} else if (ndx instanceof ASTPrintStmtNode) {
				ASTPrintStmtNode nd = (ASTPrintStmtNode) ndx;
				compileExpr(nd.expr, env);
				String loop0 = freshLabel();
				String under10 = freshLabel();
				String end = freshLabel();
				String buf = "_Pi_var_buf";
				emitLDC(REG_R1, buf);
				emitRRI("add", REG_R1, REG_R1, 8);
				emitRR("mov", REG_R3, REG_DST);
				emitRI("mov", REG_R4, 16);
				emitRI("mov", REG_R9, 0);
				emitLabel(loop0);
				emitRRI("add", REG_R9, REG_R9, 1);
				emitRI("cmp", REG_R9, 9);
				emitJMP("beq", end);
				emitRRI("sub", REG_R1, REG_R1, 1);
				emitRI("mov", REG_R7, '0');
				emitRRR("udiv", REG_R5, REG_R3, REG_R4);
				emitRRR("mul", REG_R6, REG_R5, REG_R4);
				emitRRR("sub", REG_R8, REG_R3, REG_R6);
				emitRI("cmp", REG_R8, 10);
				emitJMP("bmi", under10);
				emitRI("mov", REG_R7, 'A');
				emitRI("sub", REG_R8, 10);
				emitLabel(under10);
				emitRRR("add", REG_R7, REG_R7, REG_R8);
				emitSTRB(REG_R7, REG_R1);
				emitRR("mov", REG_R3, REG_R5);
				emitRR("mov", REG_R10, REG_R5);
				emitJMP("b", loop0);
				emitLabel(end);
				emitRI("mov", REG_R7, 4);
				emitRI("mov", REG_DST, 0);
				emitRR("mov", REG_R2, REG_R9);
				emitI("swi", 0);
		} else
			throw new Error("Unknown expression: "+ndx);
	}
	
	void compileExpr(ASTNode ndx, Environment env) {
		if (ndx instanceof ASTBinaryExprNode) {
			ASTBinaryExprNode nd = (ASTBinaryExprNode) ndx;
			compileExpr(nd.lhs, env);
			emitPUSH(REG_R1);
			emitRR("mov", REG_R1, REG_DST);
			compileExpr(nd.rhs, env);
			//orとandの追加
			if (nd.op.equals("|"))
				emitRRR("orr", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("&"))
				emitRRR("and", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("+"))
				emitRRR("add", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("-"))
				emitRRR("sub", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("*"))
				emitRRR("mul", REG_DST, REG_R1, REG_DST);
			else if (nd.op.equals("/"))
				emitRRR("udiv", REG_DST, REG_R1, REG_DST);
			else
				throw new Error("Unknwon operator: "+nd.op);
			emitPOP(REG_R1);
			
			//追加
		} else if (ndx instanceof ASTUnaryExprNode) {
				ASTUnaryExprNode nd = (ASTUnaryExprNode) ndx;
				compileExpr(nd.rhs, env);
				emitPUSH(REG_R1);
				emitRR("mov", REG_R1, REG_DST);
				if (nd.op.equals("~"))
					emitRR("mvn", REG_DST, REG_R1);
				else if (nd.op.equals("-")) {
					emitRR("mvn", REG_DST, REG_R1);
					emitRRI("add", REG_DST, REG_DST, 1);
				} else
					throw new Error("Unknwon operator: "+nd.op);
				emitPOP(REG_R1);
				
		} else if (ndx instanceof ASTNumberNode) {
			ASTNumberNode nd = (ASTNumberNode) ndx;
			emitLDC(REG_DST, nd.value);
		} else if (ndx instanceof ASTVarRefNode) {
			ASTVarRefNode nd = (ASTVarRefNode) ndx;
			Variable var = env.lookup(nd.varName);
			if (var == null)
				throw new Error("Undefined variable: "+nd.varName);
			if (var instanceof GlobalVariable) {
				GlobalVariable globalVar = (GlobalVariable) var;
				emitLDC(REG_DST, globalVar.getLabel());
				emitLDR(REG_DST, REG_DST, 0);
			} else
				throw new Error("Not a global variable: "+nd.varName);
		} else 
			throw new Error("Unknown expression: "+ndx);
	}
	
	void compile(ASTNode ast) {
		Environment env = new Environment();
		ASTProgNode prog = (ASTProgNode) ast;
		System.out.println("\t.section .data");
		System.out.println("\t@大域変数の定義");
		for (String varName: prog.varDecls) {
			if (env.lookup(varName) != null)
				throw new Error("Variable redefined: "+varName);
			GlobalVariable v = addGlobalVariable(env, varName);
			emitLabel(v.getLabel());
			System.out.println("\t.word 0");
		}
		if (env.lookup("answer") == null) {
			GlobalVariable v = addGlobalVariable(env, "answer");
			emitLabel(v.getLabel());
			System.out.println("\t.word 0");
		}
		
		//print 追加
		if (env.lookup("print") == null) {
			GlobalVariable v = addGlobalVariable(env, "buf");
			emitLabel(v.getLabel());
			System.out.println("\t.space\t8, 0");
			System.out.println("\t.byte\t0x0a");
		}
		
		System.out.println("\t.section .text");
		System.out.println("\t.global _start");
		System.out.println("_start:");
		System.out.println("\t@式をコンパイルした命令列");
		compileStmt(prog.stmt, env);
		System.out.println("\t@ EXITシステムコール");
		GlobalVariable v = (GlobalVariable) env.lookup("answer");
		emitLDC(REG_DST, v.getLabel());  //変数answerの値をr0 (終了コード)に入れる
		emitLDR("r0", REG_DST, 0);
		emitRI("mov", "r7", 1);
		// EXITのシステムコール番号
		emitI("swi", 0);
	}

	public static void main(String[] args) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(System.in);
		TinyPiSLexer lexer = new TinyPiSLexer(input);
		CommonTokenStream token = new CommonTokenStream(lexer);
		TinyPiSParser parser = new TinyPiSParser(token);
		ParseTree tree = parser.prog();
		ASTGenerator astgen = new ASTGenerator();
		ASTNode ast = astgen.translate(tree);
		Compiler compiler = new Compiler();
		compiler.compile(ast);
	}
}
