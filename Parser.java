package cop5556fa17;



import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
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
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;

	}
	
	boolean varType(){
		return t.kind==Kind.KW_boolean || t.kind==Kind.KW_int;
	}
	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		 
		if(t.kind!=Kind.IDENTIFIER) throw new SyntaxException(t,"Expect at least one identifier in the program");
		Token firstToken = t;
		Token name = t;
		ArrayList<ASTNode> decs = new ArrayList<>();
				consume();
		if(t.kind!=Kind.EOF)
			if(t.kind==Kind.IDENTIFIER||t.kind==Kind.KW_file||t.kind==Kind.KW_image||t.kind==Kind.KW_int||t.kind==Kind.KW_boolean||t.kind==Kind.KW_url){
				while(t.kind==Kind.IDENTIFIER||t.kind==Kind.KW_file||t.kind==Kind.KW_image||t.kind==Kind.KW_int||t.kind==Kind.KW_boolean||t.kind==Kind.KW_url){
					if(t.kind==Kind.IDENTIFIER){
						
						//consume();
						decs.add(statement());
					}
					else{
						//name = t;
						decs.add(declaration());
					}
					if(t.kind!=Kind.SEMI)
						throw new SyntaxException(t,"Expecting a semicolon");
					else consume();
				}
			}
			else throw new SyntaxException(t,"Expected an identifier");
		return new Program(firstToken,name,decs);
	}
	private Statement statement() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		consume();
		if(t.kind==Kind.OP_LARROW){
			return imgInStmt(first);
		}
		else if(t.kind==Kind.OP_RARROW){
			return imgOutStmt(first);
		}
		else return assignmentStmt(first);
		//return true;
		
	}

	private Statement_Out imgOutStmt(Token name) throws SyntaxException {
		// TODO Auto-generated method stub
		consume();
		Token first = t;
		//Token name = t;
		//consume();
		Sink s = sink();
		return new Statement_Out(name,name,s);
	}

	private Sink sink() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Token name = t;
		if(t.kind==Kind.IDENTIFIER)
		{
			
			consume();
			return new Sink_Ident(first,name);
		}
		else if(t.kind==Kind.KW_SCREEN){
			consume();
			return new Sink_SCREEN(first);
		}
		else
			throw new SyntaxException(t,"Identifier or KW_Screen expected");
		
	}

	private Statement_In imgInStmt(Token name) throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		//Token name = t;
		
		consume();
		//consume();
		Source s = source();
		return new Statement_In(name,name,s);
	}

	private Source source() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first =t;
		Token name = t;
		if(t.kind==Kind.IDENTIFIER)
		{
			consume();
			return new Source_Ident(first,name);
		}
		else if (t.kind==Kind.STRING_LITERAL){
			consume();
			return new Source_StringLiteral(first,first.getText());
		}
		else if(t.kind==Kind.OP_AT){
			
			consume();
			Expression e = expression();
			return new Source_CommandLineParam(first,e);
		}
		else
			throw new SyntaxException(t,"Identifier or String Literal or @ expected");
	}

	Declaration declaration() throws SyntaxException{
		
		Declaration d =null;
		Token first = t;
		if(t.kind == Kind.KW_int || t.kind == Kind.KW_boolean)
		{
			 Token type = t;
			 
			//consume();
			Token name = t;
			d =varDec();
		}
		else if(t.kind == Kind.KW_image){
			Token name  = t;
			consume();
			d = imageDec(name);
			
		}
		else if(t.kind == Kind.KW_file || t.kind == Kind.KW_url){
			Token name = t;
			consume();
			d = sourlinkDec(name);
		}
		return d;
	}
	Declaration_Variable varDec() throws SyntaxException {
		
		Token first = t;
		
		Declaration_Variable d = null;
		Expression e=null;
		consume();
		Token name = t;
		
		if(t.kind != Kind.IDENTIFIER){
			throw new SyntaxException(t, "Identifier expected at line" + t.line + " at pos " + t.pos_in_line);
		}
		else {
			
			consume();
			if(t.kind == Kind.OP_ASSIGN) {
				consume();
				e = expression();
				if(e==null) throw new SyntaxException(t,"Null expression");
			}
		}
		d = new Declaration_Variable(first,first,name,e);
		return d;
	}

	private Declaration_SourceSink sourlinkDec(Token first) throws SyntaxException {
		// TODO Auto-generated method stub
		Token type = first;
		Source s = null;
		Token name = t;
		if(t.kind==Kind.IDENTIFIER){
			
			consume();
			if(t.kind==Kind.OP_ASSIGN){
				consume();
				s = source();
			}
			else
				throw new SyntaxException(t,"Assignment operator expected");
		}
		else
			throw new SyntaxException(t,"Identifier expected");
		return new Declaration_SourceSink(first,first,name,s);

	}
   

	private Declaration_Image imageDec(Token first) throws SyntaxException {
		// TODO Auto-generated method stub
		Expression x = null;
		Expression y = null;
		Token name = null;
		if(t.kind==Kind.LSQUARE){
			consume();
			x = expression();
			if(t.kind!=Kind.COMMA){
				throw new SyntaxException(t,"Comma expected");
			}
			else{
				consume();
				y= expression();
				if(t.kind!=Kind.RSQUARE){
					throw new SyntaxException(t,"RSquare expected");
				
				}
				else{
					consume();
					//return;
				}
			}
		}
		Source s = null;
		if(t.kind!=Kind.IDENTIFIER)
			throw new SyntaxException(t,"Identifier expected");
		else{
			name = t;
			consume();
			if(t.kind==Kind.OP_LARROW){
				consume();
				s = source();
			}
			
		}
		return new Declaration_Image(first,x,y,name,s);
	}
	/*UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | ε )
FunctionApplication ::= FunctionName LPAREN Expression RPAREN  
| FunctionName  LSQUARE Selector RSQUARE 
FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs 
| KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
*/
	private Expression UnaryExpressionNotPlusMinus () throws SyntaxException{
		
		if(t.kind==Kind.OP_EXCL){
			Token first = t;
			Token op = t;
			Expression e =null;
			consume();
			Expression u = unaryExp();
			if(u!=null)
			{
				e = new Expression_Unary(first,op,u);
			}
			return e;
		}
		else if(t.kind==Kind.INTEGER_LITERAL){
			int value = Integer.parseInt(t.getText());
			Expression_IntLit l = new Expression_IntLit(t,value); 
			consume();
			return l;
		}
		else if(t.kind==Kind.LPAREN){
			consume();
			Expression e = expression();
			if(t.kind!=Kind.RPAREN)
				throw new SyntaxException(t,"RParen expected");
			else {
				consume();
				return e;
			}
			
		}
		else if(t.kind==Kind.KW_sin ||t.kind== Kind.KW_cos ||t.kind== Kind.KW_atan ||t.kind== Kind.KW_abs||t.kind== Kind.KW_cart_x || t.kind== Kind.KW_cart_y ||t.kind== Kind. KW_polar_a ||t.kind== Kind. KW_polar_r){
			Token first = t;
			consume();
			if(t.kind==Kind.LPAREN){
				Expression_FunctionAppWithExprArg exp = null;
				consume();
				Expression m = expression();
				if(t.kind!=Kind.RPAREN)
					throw new SyntaxException(t,"RParen expected");
				else consume();
				exp = new Expression_FunctionAppWithExprArg(first,first.kind,m);
				return exp;
			}
			else if(t.kind ==Kind.LSQUARE){
				Expression_FunctionAppWithIndexArg exp = null;
				consume();
				Index i = selector();
				if(t.kind!=Kind.RSQUARE)
					throw new SyntaxException(t,"RSquare expected");
				else consume();
				exp = new Expression_FunctionAppWithIndexArg(first,first.kind,i);
				return exp;
			}
		}
		else if(t.kind==Kind.BOOLEAN_LITERAL)
		{
			boolean value = t.getText().equals("true")?true:false;
			Expression_BooleanLit b = new Expression_BooleanLit(t,value);
			consume();
			return b;
		}
		else if(t.kind==Kind.KW_x ||t.kind==Kind.KW_y ||t.kind==Kind.KW_r ||t.kind==Kind.KW_a||t.kind==Kind.KW_X ||t.kind==Kind.KW_Y ||t.kind==Kind.KW_Z ||t.kind==Kind.KW_A ||t.kind==Kind.KW_R ||t.kind==Kind.KW_DEF_X ||t.kind==Kind.KW_DEF_Y)
		{
			Expression p = new Expression_PredefinedName(t,t.kind);
			consume();
			return p;
		}
		else if(t.kind==Kind.IDENTIFIER){
			Token first = t;
			Expression p = null;
			consume();
			if(t.kind!=Kind.EOF && t.kind==Kind.LSQUARE){
				//consume();//this consumes the identifier
				consume();//this consumes LSquare
				Index i = selector();
				if(t.kind!=Kind.RSQUARE)
					throw new SyntaxException(t,"RSquare expected");
				else consume();
				p = new Expression_PixelSelector(first,first,i);
			}
			else{
				//consume();
				return new Expression_Ident(first,first);
			}
			return p;
		}
			return null;
	}

		
	
	
	private Expression unaryExp() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Token op = null;
		if(t.kind==Kind.OP_MINUS||t.kind==Kind.OP_PLUS)
		{
			op = t;
			consume();
			Expression e = unaryExp();
			if(e==null) return null;//hrow new SyntaxException(t,"exp Expected");
			return new Expression_Unary(first,op,e);
		}
		else
			return UnaryExpressionNotPlusMinus();
	}

	Expression orExp() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one = andExp();
		if (one!=null) {
			while (t.kind == Kind.OP_OR) {
				op = t;
				consume();
				two = andExp();
				if (two != null) {
					one = new Expression_Binary(first, one, op, two);
				} 
				else
					return null;

			} 
		}
		return one;

	}
	 Expression andExp() throws SyntaxException{
		 Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one = eqExp();
		if(one!=null){
			while(t.kind==Kind.OP_AND){
			op = t;
			consume();
			two = eqExp();
			if(two!=null){
				one = new Expression_Binary(first,one,op,two);
			}
			else return null;
			}
			
		}
		return one;

	}
	Expression eqExp() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one = relExp();
		if(one!=null){
			while(t.kind==Kind.OP_EQ||t.kind==Kind.OP_NEQ){
			op = t;
			consume();
			two = relExp();
			if(two!=null){
				one = new Expression_Binary(first,one,op,two);
			}
			else return null;
			}
		}
		
		return one;

	}
	Expression relExp() throws SyntaxException{
		Expression one = null;
		Expression two = null;
		 Token first = t;
		Token op = null;
		one = addExp();
		if(one!=null){
			while(t.kind==Kind.OP_GE||t.kind==Kind.OP_GT||t.kind==Kind.OP_LT||t.kind==Kind.OP_LE){
			op = t;
			consume();
			two = addExp();
			if(two!=null){
				one = new Expression_Binary(first,one,op,two);
			}
			else return null;
			}
		}
		return one;

	}
	 Expression addExp() throws SyntaxException{
		 Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		 one = multi();
		if (one!=null) {
			while (t.kind == Kind.OP_PLUS || t.kind == Kind.OP_MINUS) {
				op = t;
				consume();
				two = multi();
				if (two != null) {
					one = new Expression_Binary(first, one, op, two);
				} else
					return null;
			} 
		}
		return one;
	}
	 Expression multi() throws SyntaxException{
		 Token first = t;
		 Expression one = unaryExp();
		Expression two = null;
		Token op = null;
		if (one!=null) {
			while (t.kind == Kind.OP_TIMES || t.kind == Kind.OP_DIV || t.kind == Kind.OP_MOD) {
				op = t;
				consume();
				two = unaryExp();
				if(two!=null){
					one = new Expression_Binary(first,one,op,two);
				}
				else return null;
			} 
		}
		return one;
	}
	 Statement_Assign assignmentStmt(Token name) throws SyntaxException{
		 Expression e = null;
		 Token first = name;
		 //Token name = t;
		 //consume();
		 LHS l = lhs(name);
			
			if(t.kind==Kind.OP_ASSIGN){
				consume();
				 e = expression();
				 if(e==null)
					 throw new SyntaxException(t,"Expression expected");			}
			else
			throw new SyntaxException(t,"Assignment operator expected");
			return new Statement_Assign(name,l,e);
	}

	 LHS lhs(Token name) throws SyntaxException {
		 Token first = t;
		 if(t.kind!=Kind.LSQUARE){
			 return new LHS(first,name,null);
		 }
		consume();
		 LHS l = null;
		 
		 Index i = null;
		if(t.kind==Kind.LSQUARE){
			//consume();
			i = lhsSelector();
			if(t.kind!=Kind.RSQUARE)
				throw new SyntaxException(t,"Rsquare expected");
			else{
				consume();
				return new LHS(first,name,i);
			}
			
		}
		else
			return new LHS(first,name,i);
	}
	
	Index lhsSelector() throws SyntaxException{
		Index i = null;
		Token first = t;
			consume();
			if(t.kind==Kind.KW_x){
				i = xySelector();
			}
			else
				i = raSelector();
			
			if(t.kind==Kind.RSQUARE){
				consume();
					return i;
			}
			else
				throw new SyntaxException(t,"Rsquare expected");
			
			//else throw new SyntaxException(t,"Incomplete lhsSelector");
		
	}
	 Index xySelector() throws SyntaxException{
		 Index i = null;
		 Expression e = null;
		 Token first = t; Expression two = null;
			if(t.kind!=Kind.KW_x){
				
				 throw new SyntaxException(t,"KW_x expected");
			}
			else
			{
				e = new Expression_PredefinedName(first,first.kind);
				consume();
				if(t.kind!=Kind.COMMA)
					 throw new SyntaxException(t,"Comma expected");
				else
				{
					consume();
					if(t.kind!=Kind.KW_y)
						 throw new SyntaxException(t,"Comma expected");
					else{
						two = new Expression_PredefinedName(t,t.kind);
						consume();
						i = new Index(first,e,two);
					}
				}
					

			}
			return i;
	}
	Index raSelector() throws SyntaxException{
		Expression one = null;
		Token first = t;
				Expression two = null;
		if(t.kind!=Kind.KW_r){
			 throw new SyntaxException(t,"KW_r expected");
		}
		else
		{
			one = new Expression_PredefinedName(t,t.kind);
			consume();
			if(t.kind!=Kind.COMMA)
				 throw new SyntaxException(t,"Comma expected");
			else
			{
				consume();
				if(t.kind!=Kind.KW_a)
					 throw new SyntaxException(t,"KW_A expected");
				else{
					two = new Expression_PredefinedName(t,t.kind);
					consume();
					
				}
			}
				return new Index(first,one,two);

		}
	}
	private Index selector() throws SyntaxException {
		Token first  = t;
		// TODO Auto-generated method stub
		Expression e = expression();
		Expression b = null;
		if(t.kind!=Kind.COMMA)
			throw new  SyntaxException(t,"Comma expected");
		else{
			consume();
			b = expression();
		}
		return new Index(first,e,b);
	}

	 
	Token consume(){
		t = this.scanner.nextToken();
		return t;
	}
	void match(Kind k) throws SyntaxException{
		if(t.kind==k)
			consume();
		else
			throw new SyntaxException(t,"Error in syntax");
	}
	

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * @return 
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Token first = t;
		Expression one = orExp();
		if(one==null) throw new SyntaxException(t,"exp Expected");
		Expression two = null;
		Expression three = null;
		if(t.kind==Kind.OP_Q){
			consume();
			two = expression();
			if(t.kind!=Kind.OP_COLON){
				throw new SyntaxException(t,"Colon Expected");
			}
			else{
				consume();
				three = expression();
			}
				
		}
		else return one;
		return new Expression_Conditional(first,one,two,three);
		//throw new UnsupportedOperationException();
	}



	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token readNext() throws SyntaxException {
		Token token = t;
		t = scanner.nextToken();
		return token;
	}
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
