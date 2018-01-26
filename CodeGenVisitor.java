package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
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
/*import cop5556fa17.image.ImageFrame;
import cop5556fa17.image.ImageSupport;*/

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;


	//	 java.awt.image.BufferedImage imageFile;
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");

		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);

		//terminate construction of main method
		mv.visitEnd();

		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}
	//FieldVisitor fv;
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		String type = getType(declaration_Variable.t2);
		String name = declaration_Variable.name;
		Object initValue = getInit(declaration_Variable.t2); 
		FieldVisitor fv = cw.visitField(ACC_STATIC,name, type, null, initValue);
		fv.visitEnd();
		if(declaration_Variable.e!=null){
			declaration_Variable.e.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, name, type); 

		}
		return declaration_Variable;
	}

	private Object getInit(Type t2) {
		// TODO Auto-generated method stub
		if(t2==Type.INTEGER)
			return new Integer(0);
		else if(t2==Type.BOOLEAN)
			return new Boolean(true);
		return null;
	}

	private String getType(Type t2) {
		if(t2==Type.INTEGER)
			return "I";
		else if(t2==Type.BOOLEAN)
			return "Z";
		else if(t2==Type.IMAGE)
			return ImageSupport.ImageDesc;
		else if(t2==Type.FILE||t2==Type.URL)
			return "Ljava/lang/String;";
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Kind op = expression_Binary.op;
		e0.visit(this, arg);
		e1.visit(this, arg);
		Label l1 = new Label();
		Label l2 = new Label();
		switch(op){
		case OP_PLUS:
			mv.visitInsn(IADD);
			break;
		case OP_MINUS:
			mv.visitInsn(ISUB);
			break;
		case OP_OR:
			mv.visitInsn(IOR);
			break;
		case OP_AND:
			mv.visitInsn(IAND);
			break;
		case OP_DIV:
			mv.visitInsn(IDIV);
			break;
		case OP_TIMES:
			mv.visitInsn(IMUL);
			break;
		case OP_MOD:
			mv.visitInsn(IREM);
			break;
		case OP_EQ:
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitLdcInsn(false);
			break;
		case OP_NEQ:
			mv.visitJumpInsn(IF_ICMPNE, l1);
			mv.visitLdcInsn(false);
			break;
		case OP_LT:{
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitLdcInsn(false);
			break;
		}
		case OP_LE:{
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitLdcInsn(false);
			break;
		}

		case OP_GT:{
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitLdcInsn(false);
			break;
		}
		case OP_GE:{
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitLdcInsn(false);
			break;
		}

		default:
		}
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		mv.visitLdcInsn(true);
		mv.visitLabel(l2);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		Expression e = expression_Unary.e;
		e.visit(this, arg);
		Label trueLabel= new Label();
		Label endTrue = new Label();
		String type = getType(expression_Unary.type);
		if(expression_Unary.op==Scanner.Kind.OP_PLUS){
			//mv.visitJumpInsn(GOTO, endTrue);	
		}
		else if(expression_Unary.op==Scanner.Kind.OP_MINUS){
			mv.visitInsn(INEG);
			//mv.visitJumpInsn(GOTO, endTrue);
		}
		else if(expression_Unary.op==Scanner.Kind.OP_EXCL){
			//! of a number or an expression
			//xor with int max
			if(type=="I"){
				mv.visitLdcInsn(INTEGER.MAX_VALUE);
				mv.visitInsn(IXOR);
				mv.visitJumpInsn(GOTO, endTrue);
			}
			else if(type=="Z"){
				mv.visitJumpInsn(IFEQ, trueLabel);//if true jump to true label and put false on the stack
				mv.visitLdcInsn(false);
			}


		}

		mv.visitJumpInsn(GOTO, endTrue);	


		mv.visitLabel(trueLabel);
		mv.visitLdcInsn(true);
		mv.visitLabel(endTrue);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.type);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		//TODO HW6
		if(!index.isCartesian){
			Expression e0= index.e0;
			Expression e1 = index.e1;
			e0.visit(this, arg);
			e1.visit(this, arg);
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,false);
			mv.visitInsn(DUP2_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,false);
		}
		return null;	
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		String name = expression_PixelSelector.name;
		mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
		Index i = expression_PixelSelector.index;
		Expression e1 = i.e0;
		Expression e2 = i.e1;
		e1.visit(this, arg);
		e2.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig,false);
		return null;	
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		Expression c = expression_Conditional.condition;
		c.visit(this, arg);
		Expression e1 = expression_Conditional.trueExpression;
		Expression e2 = expression_Conditional.falseExpression;
		mv.visitJumpInsn(IFEQ, falseLabel);
		e1.visit(this, arg);
		mv.visitJumpInsn(GOTO, trueLabel);
		mv.visitLabel(falseLabel);
		e2.visit(this, arg);
		mv.visitLabel(trueLabel);

		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		String name = declaration_Image.name;
		Expression e1 = declaration_Image.xSize;
		Expression e2 = declaration_Image.ySize;
		FieldVisitor fv = cw.visitField(ACC_STATIC,name, ImageSupport.ImageDesc, null, null);
		fv.visitEnd();
		if(declaration_Image.source==null){
			if(e1!=null){
				e1.visit(this, arg);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, 3);//TODO
				//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);
			}else{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 3);
				mv.visitLdcInsn(256);
			}
			if(e2!=null){
				e2.visit(this, arg);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE,4 );//TODO
				//				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);

			}else{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 4);
				mv.visitLdcInsn(256);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig,false);
		}
		else{
			Source s = declaration_Image.source;
			s.visit(this, arg);
			if(e1!=null){
				e1.visit(this, arg);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, 3);//TODO
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);

			}
			else{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 3);//TODO
				mv.visitInsn(ACONST_NULL);

			}
			if(e2==null){
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE,4);//TODO
				mv.visitInsn(ACONST_NULL);

			}
			else{
				e2.visit(this, arg);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE,4 );//TODO
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);

			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, name, ImageSupport.ImageDesc);
		return null;	
	}


	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {

		String nameOfFile = source_StringLiteral.fileOrUrl;
		mv.visitLdcInsn(nameOfFile);
		return null;	
	}



	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 

		String type = getType(source_CommandLineParam.type);
		Expression e = source_CommandLineParam.paramNum;
		mv.visitVarInsn(ALOAD, 0);
		e.visit(this, arg);
		mv.visitInsn(AALOAD);
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		String name = source_Ident.name;
		String type = getType(source_Ident.type);
		//if(type!=null)
		mv.visitFieldInsn(GETSTATIC, className, name, type);
		/*	else
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
		 */

		return null;	
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		//TODO HW6
		String name = declaration_SourceSink.name;
		String type = getType(declaration_SourceSink.t2);
		FieldVisitor fv = cw.visitField(ACC_STATIC,name, type, null, null);
		fv.visitEnd();
		Source s = declaration_SourceSink.source;
		if(s!=null)
		{
			s.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, name, type);
		}
		return null;

	}



	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		int val = expression_IntLit.value;
		mv.visitLdcInsn(val);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		Expression e = expression_FunctionAppWithExprArg.arg;
		Kind function = expression_FunctionAppWithExprArg.function;
		e.visit(this, arg);
		if(function==Kind.KW_abs){
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig,false);

		}
		else if(function==Kind.KW_log){
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig,false);

		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		//TODO HW6
		Index i = expression_FunctionAppWithIndexArg.arg;
		Expression e0 = i.e0;
		e0.visit(this, arg);
		Expression e1 = i.e1;
		e1.visit(this, arg);
		Kind function = expression_FunctionAppWithIndexArg.function;
		switch(function){
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);

			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);
			break;
		}

		return null;

	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		switch(expression_PredefinedName.kind){
		case KW_x:
			mv.visitVarInsn(ILOAD, 1);
			break;
		case KW_y:
			mv.visitVarInsn(ILOAD, 2);

			break;
		case KW_X:
			mv.visitVarInsn(ILOAD, 3);

			break;
		case KW_Y:
			mv.visitVarInsn(ILOAD, 4);

			break;
		case KW_r:
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r",RuntimeFunctions.polar_rSig, false);

			break;
		case KW_a:
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a",RuntimeFunctions.polar_aSig, false);

			break;
		case KW_R:
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r",RuntimeFunctions.polar_rSig, false);


			break;
		case KW_A:
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a",RuntimeFunctions.polar_aSig, false);


			break;
		case KW_DEF_X:
			mv.visitLdcInsn(new Integer(256));
			break;
		case KW_DEF_Y:
			mv.visitLdcInsn(new Integer(256));

			break;
		case KW_Z:
			mv.visitLdcInsn(new Integer(16777215));

			break;
		}
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		String name = statement_Out.name;
		String type = getType(statement_Out.getDec().t2);


		//uncomment later
		String desc = "java/io/PrintStream";
		//(GRADE, mv, "");
		if(type=="I"||type=="Z"){
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, name, type);
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().t2);
			mv.visitMethodInsn(INVOKEVIRTUAL, desc, "println", "("+type+")V",false);
		}

		else if(statement_Out.getDec().t2==TypeUtils.Type.IMAGE){
			//TODO check what load image 
			mv.visitFieldInsn(GETSTATIC, className, name, type);
			CodeGenUtils.genLogTOS(GRADE, mv, TypeUtils.Type.IMAGE);
			Sink s = statement_Out.sink;
			s.visit(this, arg);
		}

		return statement_Out;
	}


	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		String name = statement_In.name;
		String type = getType(statement_In.getDec().t2);
		String descriptorBool ="java/lang/Boolean";
		String decsriptorInt = "java/lang/Integer";
		Source s = statement_In.source;
		Label first = new Label();
		Label second = new Label();
		s.visit(this,arg);
		if(type=="I")
		{	mv.visitMethodInsn(INVOKESTATIC,decsriptorInt , "parseInt", "(Ljava/lang/String;)I",false);
		//mv.visitFieldInsn(PUTSTATIC, className, name, type);

		} 
		else if(type=="Z")
		{
			mv.visitMethodInsn(INVOKESTATIC,descriptorBool , "parseBoolean", "(Ljava/lang/String;)Z",false);
		}else if(statement_In.getDec().t2==TypeUtils.Type.IMAGE){

			mv.visitVarInsn(ILOAD,3);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(IF_ICMPEQ, first);
			mv.visitVarInsn(ILOAD,3);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);

			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;",false);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);

			mv.visitJumpInsn(GOTO,second);
			mv.visitLabel(first);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitLabel(second);
		}
		mv.visitFieldInsn(PUTSTATIC, className, name, type);

		return statement_In;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		//throw new UnsupportedOperationException();
		String type = getType(statement_Assign.lhs.type);
		if(type=="I"||type=="Z"){
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		else if(statement_Assign.lhs.type==TypeUtils.Type.IMAGE){
			String name = statement_Assign.lhs.name;
			Label xStart = new Label();
			Label yStart = new Label();
			Label yEnd = new Label();
			Label xEnd = new Label();
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX",ImageSupport.getXSig, false);

			mv.visitVarInsn(ISTORE,3);//store X
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY",ImageSupport.getYSig, false);

			mv.visitVarInsn(ISTORE,4);//store Y
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitJumpInsn(GOTO, xStart);
			mv.visitLabel(yStart);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			mv.visitInsn(ICONST_0);                //j=0
			mv.visitVarInsn(ISTORE, 2);
			mv.visitJumpInsn(GOTO, yEnd);
			mv.visitLabel(xEnd);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);

			mv.visitIincInsn(2, 1);//next iteration

			mv.visitLabel(yEnd);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPLT, xEnd);

			mv.visitIincInsn(1, 1);
			mv.visitLabel(xStart);

			mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);

			mv.visitVarInsn(ILOAD, 1); 
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPLT, yStart);                    

		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		String type = getType(lhs.type);
		String name = lhs.name;
		Index i = lhs.index;

		if(type=="I"||type=="Z"){
			mv.visitFieldInsn(PUTSTATIC, className, name, type);
		}
		
		else if( lhs.type==TypeUtils.Type.IMAGE){
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig,false);
		}
		return lhs;
	}


	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className, "makeFrame", ImageSupport.makeFrameSig,false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		String name  = sink_Ident.name;
		String type = getType(sink_Ident.type);
		mv.visitFieldInsn(GETSTATIC, className, name, type);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write",ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		String type = getType(expression_BooleanLit.type);
		Boolean v = expression_BooleanLit.value;
		mv.visitLdcInsn(v);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		String name = expression_Ident.name;
		String type = getType(expression_Ident.type);
		mv.visitFieldInsn(GETSTATIC, className, name, type);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.type);
		return null;
	}

	/*@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}*/

}
