import java.util.ArrayList;
	import org.antlr.v4.runtime.tree.ParseTree;
	import org.antlr.v4.runtime.tree.TerminalNode;

import parser.TinyPiSParser.AndExprContext;
import parser.TinyPiSParser.NotExprContext;
import parser.TinyPiSParser.OrExprContext;
import parser.TinyPiSParser.AddExprContext;
import parser.TinyPiSParser.ExprContext;
import parser.TinyPiSParser.IfStmtContext;
import parser.TinyPiSParser.LiteralExprContext;
import parser.TinyPiSParser.MulExprContext;
import parser.TinyPiSParser.ParenExprContext;
import parser.TinyPiSParser.VarExprContext;
import parser.TinyPiSParser.WhileStmtContext;
import parser.TinyPiSParser.ProgContext;
import parser.TinyPiSParser.StmtContext;
import parser.TinyPiSParser.CompoundStmtContext;
import parser.TinyPiSParser.AssignStmtContext;
import parser.TinyPiSParser.PrintStmtContext;
	
public class ASTGenerator {	
	ASTNode translateExpr(ParseTree ctxx) {
		
			    //プログラム		
		if (ctxx instanceof ProgContext) {
			ProgContext ctx = (ProgContext) ctxx;
			ArrayList<String> varDecls = new ArrayList<String>();
			for (TerminalNode token: ctx.varDecls().IDENTIFIER())
				varDecls.add(token.getText());
			ASTNode stmt = translate(ctx.stmt());
			return new ASTProgNode(varDecls, stmt);	
						
			//複合文
		} else if (ctxx instanceof CompoundStmtContext) {
			CompoundStmtContext ctx = (CompoundStmtContext) ctxx;
			ArrayList<ASTNode> stmts = new ArrayList<ASTNode>();
			for (StmtContext t: ctx.stmt()) {
				ASTNode n = translate(t);
				stmts.add(n);
			}
		return new ASTCompoundStmtNode(stmts);
			//代入文
		} else if (ctxx instanceof AssignStmtContext) {
			AssignStmtContext ctx = (AssignStmtContext) ctxx;
			String var = ((ParseTree) ctx.IDENTIFIER()).getText();
			ASTNode expr = translate(ctx.expr());
			return new ASTAssignStmtNode(var, expr);
		}
		
		//if文
		else if (ctxx instanceof IfStmtContext){
			IfStmtContext ctx = (IfStmtContext) ctxx;
			ASTNode cond = translate(ctx.expr());
			ASTNode thenClause = translate(ctx.stmt(0));
			ASTNode elseClause = translate(ctx.stmt(1));
			return new ASTIfStmtNode(cond, thenClause, elseClause);
		}
				
		//while文
		else if (ctxx instanceof WhileStmtContext){
			WhileStmtContext ctx =(WhileStmtContext) ctxx;
			ASTNode cond = translate(ctx.expr());
			ASTNode stmt = translate(ctx.stmt());
			return new ASTWhileStmtNode(cond, stmt);
		}
		
		//演習13print追加
		else	if (ctxx instanceof PrintStmtContext) {
			PrintStmtContext ctx = (PrintStmtContext) ctxx;
			ASTNode expr = translate(ctx.expr());
			return new ASTPrintStmtNode(expr);
		}
				

		else	if (ctxx instanceof ExprContext) {
			ExprContext ctx = (ExprContext) ctxx;
			return translateExpr(ctx.orExpr());
		} 
			//or
		else if (ctxx instanceof OrExprContext) {
			OrExprContext ctx = (OrExprContext) ctxx;
			if (ctx.orExpr() == null)
				return translateExpr(ctx.andExpr());
			ASTNode lhs = translateExpr(ctx.orExpr());
			ASTNode rhs = translateExpr(ctx.andExpr());
			return new ASTBinaryExprNode(ctx.OROP().getText(), lhs, rhs);
		} 
			//and
		else if (ctxx instanceof AndExprContext) {
			AndExprContext ctx = (AndExprContext) ctxx;
			if (ctx.andExpr() == null)
				return translateExpr(ctx.addExpr());
			ASTNode lhs = translateExpr(ctx.andExpr());
			ASTNode rhs = translateExpr(ctx.addExpr());
			return new ASTBinaryExprNode(ctx.ANDOP().getText(), lhs, rhs);
		} 
			//add
		else if (ctxx instanceof AddExprContext) {
			AddExprContext ctx = (AddExprContext) ctxx;
			if (ctx.addExpr() == null)
				return translateExpr(ctx.mulExpr());
			String op;
			if (ctx.ADDOP()==null){
				op = ctx.SUBOP().getText();
			} else {
				op = ctx.ADDOP().getText();
			}
			ASTNode lhs = translateExpr(ctx.addExpr());
			ASTNode rhs = translateExpr(ctx.mulExpr());
			return new ASTBinaryExprNode(op, lhs, rhs);
		} else if (ctxx instanceof MulExprContext) {
			MulExprContext ctx = (MulExprContext) ctxx;
			if (ctx.mulExpr() == null)
				return translateExpr(ctx.unaryExpr());
			ASTNode lhs = translateExpr(ctx.mulExpr());
			ASTNode rhs = translateExpr(ctx.unaryExpr());
			return new ASTBinaryExprNode(ctx.MULOP().getText(), lhs, rhs);
		} 
		//not
		else if (ctxx instanceof NotExprContext) {
			NotExprContext ctx = (NotExprContext) ctxx;
			ASTNode rhs = translateExpr(ctx.unaryExpr());
			String op;
			if (ctx.NOTOP()==null){
				op = ctx.SUBOP().getText();
			} else {
				op = ctx.NOTOP().getText();
			}
			return new ASTUnaryExprNode( op, rhs);
						
						
		} else if (ctxx instanceof LiteralExprContext) {
			LiteralExprContext ctx = (LiteralExprContext) ctxx;
			int value = Integer.parseInt(ctx.VALUE().getText());
			return new ASTNumberNode(value);
		} else if (ctxx instanceof VarExprContext) {
			VarExprContext ctx = (VarExprContext) ctxx;
			String varName = ctx.IDENTIFIER().getText();
			return new ASTVarRefNode(varName);
		} else if (ctxx instanceof ParenExprContext) {
			ParenExprContext ctx = (ParenExprContext) ctxx;
			return translateExpr(ctx.expr());
		}
		throw new Error("Unknown parse tree node: "+ctxx.getText());		
	}
	ASTNode translate(ParseTree ctxx) {
		return translateExpr(ctxx);
	}
	
}
