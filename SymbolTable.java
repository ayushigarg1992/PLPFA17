package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.AST.Declaration;

public class SymbolTable {
	HashMap<String,Declaration> symbolTable;// = new HashMap<>(); 
	public SymbolTable(){
		symbolTable = new HashMap<>();
	}
	public TypeUtils.Type lookupType(String name){
		Declaration d = symbolTable.get(name);
		TypeUtils.Type type = null;
		if(d!=null) type=TypeUtils.getType(d.firstToken);
		return type;
	}
	public Declaration lookupDec(String name){
		return symbolTable.get(name);
	}
	public void insert(String name,Declaration d){
		symbolTable.put(name,d);
	}
}
