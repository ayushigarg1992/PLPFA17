package cop5556fa17.AST;

import cop5556fa17.TypeUtils;
import cop5556fa17.Scanner.Token;

public abstract class Sink extends ASTNode {
	public TypeUtils.Type type;
	public Sink(Token firstToken) {
		super(firstToken);
	}
	

}
