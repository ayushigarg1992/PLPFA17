package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
		SymbolTable symbols = new SymbolTable(); 
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg)throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Variable.name;
		declaration_Variable.t2 = TypeUtils.getType(declaration_Variable.firstToken);
		Expression e = declaration_Variable.e;
		if(e!=null){
			e.visit(this, null);
			if(declaration_Variable.t2 != e.type) 
				throw new SemanticException(declaration_Variable.firstToken,"Incompatible types of expression!");
		}
		if(symbols.lookupDec(name)!=null){
			throw new SemanticException(declaration_Variable.type,"Varibale declared already!");
		}
		symbols.insert(name, declaration_Variable);
		
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = expression_Binary.e0;
		e0.visit(this, null);
		Expression e1 = expression_Binary.e1;
		e1.visit(this, null);
		Kind op = expression_Binary.op;
		if(op==Kind.OP_EQ||op==Kind.OP_NEQ){
			expression_Binary.type = TypeUtils.Type.BOOLEAN;
		}
		else if((op==Kind.OP_GE||op==Kind.OP_GT||op==Kind.OP_LE||op==Kind.OP_LT)&& e0.type==TypeUtils.Type.INTEGER){
			expression_Binary.type = TypeUtils.Type.BOOLEAN;
		}
		else if((op==Kind.OP_AND || op==Kind.OP_OR)&&(e0.type==TypeUtils.Type.INTEGER || e0.type==TypeUtils.Type.BOOLEAN)){
			expression_Binary.type = e0.type;
		}
		else if(op==Kind.OP_PLUS||op==Kind.OP_MINUS|| op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_MOD){
			expression_Binary.type = TypeUtils.Type.INTEGER;
		}
		else
			expression_Binary.type=null;
		
		if(expression_Binary.type==null||e0.type!=e1.type)
		throw new SemanticException(expression_Binary.firstToken,"type mismatch error");
				return expression_Binary;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = expression_Unary.e;
		e.visit(this, null);

		TypeUtils.Type t = e.type;
		Kind op = expression_Unary.op;
		if(op==Kind.OP_EXCL){
			if(t==TypeUtils.Type.BOOLEAN||t==TypeUtils.Type.INTEGER)
				expression_Unary.type = t;
		}
		else if(op==Kind.OP_PLUS||op==Kind.OP_MINUS){
			if(t==TypeUtils.Type.INTEGER)
				expression_Unary.type= TypeUtils.Type.INTEGER;
		}
		else
			expression_Unary.type = null;
		
		if(expression_Unary.type==null)
			throw new SemanticException(expression_Unary.firstToken,"type for unary expression is expected");
		//throw new UnsupportedOperationException();
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = index.e0;
		e0.visit(this, null);
		Expression e1 = index.e1;
		e1.visit(this, null);
		if(!(e0.type==TypeUtils.Type.INTEGER && e1.type==TypeUtils.Type.INTEGER))
		throw new SemanticException(index.firstToken,"Non Integer types in expression");
		index.isCartesian = !(e0.firstToken.kind==Kind.KW_r && e1.firstToken.kind==Kind.KW_a);
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		TypeUtils.Type t = symbols.lookupType(expression_PixelSelector.name);
		Index index = (Index) visitIndex(expression_PixelSelector.index,null);
		if(t==TypeUtils.Type.IMAGE)
			expression_PixelSelector.type =  TypeUtils.Type.INTEGER;
		else if(index==null)
			expression_PixelSelector.type = t;
		else expression_PixelSelector.type = null;
		if(expression_PixelSelector.type==null) throw new SemanticException(expression_PixelSelector.firstToken,"Type must be integer or image");
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression exp_true = expression_Conditional.trueExpression;
		exp_true.visit(this, null);
		Expression exp_false = expression_Conditional.falseExpression;
		exp_false.visit(this, null);
		Expression exp_cond = expression_Conditional.condition;
		exp_cond.visit(this, null);
		if(exp_cond.type!=TypeUtils.Type.BOOLEAN||exp_true.type!=exp_false.type)
		throw new SemanticException(expression_Conditional.firstToken,"Incompatible operands in conditional");
		expression_Conditional.type = exp_true.type;
		return expression_Conditional;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Image.name;
		Expression xSize =declaration_Image.xSize;
		if(xSize!=null) xSize.visit(this, null);
		Expression ySize = declaration_Image.ySize;
		if(ySize!=null)ySize.visit(this, null);
		if(xSize!=null){
			if(ySize==null||xSize.type!=ySize.type ||xSize.type!=TypeUtils.Type.INTEGER)
				throw new SemanticException(declaration_Image.firstToken,"Incorrect image description");
			
		}
		if(symbols.lookupDec(name)!=null){
			throw new SemanticException(declaration_Image.firstToken,"Varibale declared already!");
		}
		declaration_Image.t2 = TypeUtils.Type.IMAGE;
		symbols.insert(name, declaration_Image);
		Source s = declaration_Image.source;
		if(s!=null) s.visit(this, null);
		return declaration_Image;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		String source = source_StringLiteral.fileOrUrl;
		URL url = null;
		try {
		    url = new URL(source);
		} catch (MalformedURLException e) {
		    
		}
		if(url == null){
			source_StringLiteral.type = TypeUtils.Type.FILE;
		}
		else
			source_StringLiteral.type = TypeUtils.Type.URL;
		return source_StringLiteral;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
//		Expression e = source_CommandLineParam.paramNum;
//		e.visit(this, null);
//		source_CommandLineParam.type = e.type;
//		if(source_CommandLineParam.type!=TypeUtils.Type.INTEGER)
//			throw new SemanticException(source_CommandLineParam.firstToken,"Bad command line Args");
//		return source_CommandLineParam;
		//throw new UnsupportedOperationException();
		source_CommandLineParam.type =  null;
		Expression param = source_CommandLineParam.paramNum;
		param.visit(this, arg);
		if(param.type!=TypeUtils.Type.INTEGER) 
			throw new SemanticException(source_CommandLineParam.firstToken,"Incompatible types!");
		return source_CommandLineParam.type;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		source_Ident.type = symbols.lookupType(source_Ident.name);
		if(!(source_Ident.type==TypeUtils.Type.URL||source_Ident.type==TypeUtils.Type.FILE))
			throw new SemanticException(source_Ident.firstToken,"Variable declared already!");
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_SourceSink.name;
		TypeUtils.Type type = TypeUtils.getType(declaration_SourceSink.firstToken);
		
		if(symbols.lookupDec(name)!=null){
			throw new SemanticException(declaration_SourceSink.firstToken,"Variable declared already!");
		}
		else
			symbols.insert(name, declaration_SourceSink);
		declaration_SourceSink.t2 = type;
		Source s = declaration_SourceSink.source;
		s.visit(this,null);
		if(s.type!=declaration_SourceSink.t2 && s.type!=null) throw new SemanticException(declaration_SourceSink.firstToken,"Source type incompatible");
		return declaration_SourceSink;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.type = TypeUtils.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		Expression e = expression_FunctionAppWithExprArg.arg;
		e.visit(this, null);
		if(e.type!=TypeUtils.Type.INTEGER)
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"Expected Integer");
		expression_FunctionAppWithExprArg.type=TypeUtils.Type.INTEGER;
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Index i = expression_FunctionAppWithIndexArg.arg;
		i.visit(this, null);
		expression_FunctionAppWithIndexArg.type = TypeUtils.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return expression_FunctionAppWithIndexArg;
		
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.type = TypeUtils.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		
		Declaration d = symbols.lookupDec(statement_Out.name);
		statement_Out.setDec(d);
		//statement_Out.getDec();
		//d.visit(this, null);
		Sink s = statement_Out.sink;
		s.visit(this, null);
		if(d==null) throw new SemanticException(statement_Out.firstToken,"Declaration not found");
		if(((d.t2==TypeUtils.Type.BOOLEAN||d.t2==TypeUtils.Type.INTEGER)&&s.type!=TypeUtils.Type.SCREEN )||(d.t2==TypeUtils.Type.IMAGE&& (s.type!=TypeUtils.Type.FILE &&s.type!=TypeUtils.Type.SCREEN)))
		throw new SemanticException(statement_Out.firstToken,"Invalid Sink Argument");
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		Declaration d = symbols.lookupDec(statement_In.name);
		//d.visit(this,null);
		statement_In.setDec(d);
		Source s = statement_In.source;
		s.visit(this,null);
		/*if(d==null||d.t2!=s.type)
		throw new SemanticException(statement_In.firstToken,"type mismatch in source");
		*/
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		LHS l = statement_Assign.lhs;
		l.visit(this,null);
		Expression e = statement_Assign.e;
		e.visit(this, null);
		statement_Assign.setCartesian(l.isCartesian);
		if(l.type!=e.type)
		if(!(l.type==TypeUtils.Type.IMAGE && e.type==TypeUtils.Type.INTEGER))
			throw new SemanticException(statement_Assign.firstToken,"type mismatch in assignment");
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
	 
		Declaration dec = symbols.lookupDec(lhs.name);//lhs.dec;
		if(dec==null) throw new SemanticException(lhs.firstToken,"Undeclared variable");
		//dec.visit(this, null);
		lhs.dec =dec;
		lhs.type = lhs.dec.t2;
		Index i = lhs.index;
		if(i!=null){
			i.visit(this, null);
			lhs.isCartesian = i.isCartesian();
		}
		
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.type = TypeUtils.Type.SCREEN;
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		sink_Ident.type = symbols.lookupType(sink_Ident.name);
		if(sink_Ident.type!=TypeUtils.Type.FILE){
			throw new SemanticException(sink_Ident.firstToken,"File type expected!");
		}
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.type = TypeUtils.Type.BOOLEAN;
		
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(symbols.lookupType(expression_Ident.name)==null) throw new SemanticException(expression_Ident.firstToken,"declaration not found");
		expression_Ident.type = symbols.lookupType(expression_Ident.name);
		return expression_Ident;
	}

}
