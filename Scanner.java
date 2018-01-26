/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
	public static Map<String,Kind> map = new HashMap<>();
	//class variables used during scanning
	//int posLine,position,lineNum,length;
	
	
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}
	
	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	//create a mapping for keywords and token type in Kind
	public void createMapKeyWord(){
		map.put("true",Kind.BOOLEAN_LITERAL);
		map.put("false",Kind.BOOLEAN_LITERAL);
		map.put("x",Kind.KW_x);
		map.put("X",Kind.KW_X); 
		map.put("y",Kind.KW_y); 
		map.put("Y",Kind.KW_Y); 
		map.put("r",Kind.KW_r); 
		map.put("R",Kind.KW_R); 
		map.put("a",Kind.KW_a); 
		map.put("A",Kind.KW_A);
		map.put("Z",Kind.KW_Z); 
		map.put("DEF_X",Kind.KW_DEF_X);
		map.put("DEF_Y",Kind.KW_DEF_Y);
		map.put("SCREEN",Kind.KW_SCREEN); 
		map.put("cart_x",Kind.KW_cart_x);
		map.put("cart_y",Kind.KW_cart_y);
		map.put("polar_a",Kind.KW_polar_a); 
		map.put("polar_r",Kind.KW_polar_r); 
		map.put("abs",Kind.KW_abs); 
		map.put("sin",Kind.KW_sin);
		map.put("cos",Kind.KW_cos);
		map.put("atan",Kind.KW_atan);
		map.put("log",Kind.KW_log);
		map.put("image",Kind.KW_image);
		map.put("int",Kind.KW_int); 
		map.put("boolean",Kind.KW_boolean);
		map.put("url",Kind.KW_url);
		map.put("file",Kind.KW_file);
		map.put("=",Kind.OP_ASSIGN);
		map.put(">",Kind.OP_GT);
		map.put("<",Kind.OP_LT); 
		map.put("!",Kind.OP_EXCL);
		map.put("?",Kind.OP_Q);
		map.put(":",Kind.OP_COLON); 
		map.put("==",Kind.OP_EQ); 
		map.put("!=",Kind.OP_NEQ); 
		map.put(">=",Kind.OP_GE);
		map.put("<=",Kind.OP_LE); 
		map.put("&",Kind.OP_AND);
		map.put("|",Kind.OP_OR);
		map.put("+",Kind.OP_PLUS); 
		map.put("-",Kind.OP_MINUS);
		map.put("*",Kind.OP_TIMES);
		map.put("/",Kind.OP_DIV);
		map.put("%",Kind.OP_MOD); 
		map.put("**",Kind.OP_POWER);
		map.put("@",Kind.OP_AT); 
		map.put("->",Kind.OP_RARROW); 
		map.put("<-",Kind.OP_LARROW); 
		map.put("(",Kind.LPAREN);
		map.put(")",Kind.RPAREN); 
		map.put("[",Kind.LSQUARE);
		map.put("]",Kind.RSQUARE);
		map.put(";",Kind.SEMI);
		map.put(",",Kind.COMMA);
		map.put("-1",Kind.EOF);
		
	}
	
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	Scanner(String inputString) {
		
		createMapKeyWord();	//to init the keyword and token kind mapping this method will map token values to token kind.
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		while(pos<chars.length-1){
			switch(chars[pos]){
			/*all separators start here*/
			case ' ':
				//ignore whitespace in the beginning
				while(chars[pos]==' '){
						pos++;
						posInLine++;
				}
				break;
			
			case ';':
					tokens.add(new Token(Kind.SEMI, pos++, 1, line, posInLine++));
					break;
			case ',':
				tokens.add(new Token(Kind.COMMA, pos++, 1, line, posInLine++));
				break;
			case '(':
				tokens.add(new Token(Kind.LPAREN, pos, 1, line, posInLine));
				pos++;
				posInLine++;
				break;
			case ')':
				tokens.add(new Token(Kind.RPAREN, pos++, 1, line, posInLine++));
				break;
			case '[':
				tokens.add(new Token(Kind.LSQUARE, pos++, 1, line, posInLine++));
				break;
			case ']':
				tokens.add(new Token(Kind.RSQUARE, pos++, 1, line, posInLine++));
				break;
				/*all separators end here*/
				/*all operators start here*/
			case ':':
				tokens.add(new Token(Kind.OP_COLON, pos++, 1, line, posInLine++));
				break;
			case '=':
				if(chars[pos+1]=='='){
						tokens.add(new Token(Kind.OP_EQ, pos++, 2, line, posInLine++));
						pos++;
						posInLine++;
					}
				else
					tokens.add(new Token(Kind.OP_ASSIGN, pos++, 1, line, posInLine++));

				break;
			case '>':
				
				if(chars[pos+1]=='='){
					tokens.add(new Token(Kind.OP_GE, pos++, 2, line, posInLine++));
					pos++;
					posInLine++;
				}
				
			else
				tokens.add(new Token(Kind.OP_GT, pos++, 1, line, posInLine++));				
				break;
			case '<':
				if(chars[pos+1]=='='){
					tokens.add(new Token(Kind.OP_LE, pos++, 2, line, posInLine++));
					pos++;
					posInLine++;
				}
				else if(chars[pos+1]=='-'){
					tokens.add(new Token(Kind.OP_LARROW, pos++, 2, line, posInLine++));
					pos++;
					posInLine++;
				}
				else
				tokens.add(new Token(Kind.OP_LT, pos++, 1, line, posInLine++));				
				break;
			case '!':
				if(chars[pos+1]=='='){
					tokens.add(new Token(Kind.OP_NEQ, pos++, 2, line, posInLine++));
					//System.out.println(tokens.toString());
					pos++;
					posInLine++;
				}
				else
				tokens.add(new Token(Kind.OP_EXCL, pos++, 1, line, posInLine++));				
				break;
			case '?':
				tokens.add(new Token(Kind.OP_Q, pos++, 1, line, posInLine++));
				break;
			case '-':
				if(chars[pos+1]=='>'){
					tokens.add(new Token(Kind.OP_RARROW, pos++, 2, line, posInLine++));
					pos++;
					posInLine++;
				}
				
				else
				tokens.add(new Token(Kind.OP_MINUS, pos++, 1, line, posInLine++));				
				break;
			case '+':
				tokens.add(new Token(Kind.OP_PLUS, pos++, 1, line, posInLine++));
				break;
			case '/':
				if(chars[pos+1]=='/'){
					pos++;
					while(chars[pos+1]!='\n'&& chars[pos+1]!=EOFchar){
						//pos++;
						//posInLine++;
						if(chars[pos]=='\n'){
							line++;
							pos++;
							posInLine=1;
							break;
							}
						else if(chars[pos]=='\r'){
							
								if(chars[pos+1]=='\n')
								{
									pos++;
									line++;
									posInLine=1;
								}
								else
								{
										line++;
										posInLine=1;
								}
								break;
						}
						else
							{pos++;posInLine++;}//throw new LexicalException("Has an invalid symbol in comment ",pos);
						//ignore comments and keep incrementing position
					}
					//TO-DO check corner cases here.
					pos++;
					//posInLine++;
				}
				else
					tokens.add(new Token(Kind.OP_DIV, pos++, 1, line, posInLine++));
				break;
			case '*':
				if(chars[pos+1]=='*'){
					tokens.add(new Token(Kind.OP_POWER, pos++, 2, line, posInLine++));
					pos++;
					posInLine++;
				}
				
				else
				tokens.add(new Token(Kind.OP_TIMES, pos++, 1, line, posInLine++));				
				break;
			case '&':
				tokens.add(new Token(Kind.OP_AND, pos++, 1, line, posInLine++));
				break;
			case '|':
				tokens.add(new Token(Kind.OP_OR, pos++, 1, line, posInLine++));
				break;
			case '@':
				tokens.add(new Token(Kind.OP_AT, pos++, 1, line, posInLine++));
				break;
			case '%':
				tokens.add(new Token(Kind.OP_MOD, pos++, 1, line, posInLine++));
				break;
			//escape sequences start here 
			case '\b':
				pos++;
				posInLine++;
				break;
			case '\t':
				pos++;
				posInLine++;
				break;
			case '\n':
				line++;
				pos++;
				posInLine=1;
				break;
			case '\f':
				pos++;
				posInLine++;
				break;
			case '\r':
				if(chars[pos+1]=='\n')
				{
					pos++;
					line++;
					posInLine=1;
				}
				else
				{		
						pos++;
						line++;
						posInLine=1;
				}
				break;
			//case 
			//this case is to handle String Literals	
			case '\"':
				//pos++;
				StringBuilder sb = new StringBuilder();
				
				boolean m = false;
				while( chars[pos]!=EOFchar)
				{
					
					
					if(chars[pos+1]=='\\'){
						if(!(chars[pos+2]=='\\'||chars[pos+2]=='b'||chars[pos+2]=='t'||chars[pos+2]=='n'||chars[pos+2]=='r'||chars[pos+2]=='f'))
							throw new LexicalException("Invalid character in string literal",pos);
					}
					sb.append(chars[pos++]);
					posInLine++;
					if(chars[pos]=='\"') {
						m = true; 
						sb.append(chars[pos]);
						pos++;
						posInLine++;
						
						break;
					}
				}
				if(!m) throw new LexicalException("unclosed string literal",pos);
				tokens.add(new Token(Kind.STRING_LITERAL,pos-sb.length(),sb.length(),line,posInLine-sb.length()));
				break;
				
			case '\'':
				pos++;
				posInLine++;
				break;
			default:
				if(Character.isLetter(chars[pos])||chars[pos]=='$'||chars[pos]=='_'){
					//identifier or keyword
					StringBuilder sb2 = new StringBuilder();
					//sb2.append(chars[pos]);
					while(pos<chars.length &&(chars[pos]!=' '||chars[pos]!='\n'||chars[pos]!='\r')){
						sb2.append(chars[pos++]);
						posInLine++;
						if(!(Character.isLetter(chars[pos])|| Character.isDigit(chars[pos]))&&chars[pos]!='$'&&chars[pos]!='_')
							break;
						
					}
					String word = sb2.toString().trim();
					if(map.containsKey(word))
					{
						//[KW_x,x,0,1,1,1], [EOF,,2,0,1,3]
						//the keywords or boolean literals can be handles here
						tokens.add(new Token(map.get(word),pos-word.length(),sb2.length(),line,posInLine-word.length()));
						break;
					}
					else
						//other identifiers
						tokens.add(new Token(Kind.IDENTIFIER,pos-sb2.length(),sb2.length(),line,posInLine-sb2.length()));
					break;

				}
				else if(Character.isDigit(chars[pos])){
					StringBuilder s = new StringBuilder();
					//deal with the leading zeros
					while(chars[pos]=='0'){
						tokens.add(new Token(Kind.INTEGER_LITERAL,pos++,1,line,posInLine++));
					}
					while(pos<chars.length &&(chars[pos]!=' '||chars[pos]!='\n'||chars[pos]!='\r'||chars[pos]!='\b'||chars[pos]!='\f'||chars[pos]!='\t')){
						if(!Character.isDigit(chars[pos]))
							break;
						s.append(chars[pos++]);
						posInLine++;
						if(chars[pos]==' ')
							break;
					}
					
					String word = s.toString().trim();
					word = s.toString();
					int wordLength= word.length();
					if(wordLength>10)
						throw new LexicalException("Invalid Number! Overflow",pos);
					else if(wordLength==10)
					{
						long val = Long.valueOf(word);
						if(val>2147483647)
							throw new LexicalException("Invalid Number! Overflow",pos);
						else
							tokens.add(new Token(Kind.INTEGER_LITERAL,pos-word.length(),word.length(),line,posInLine-word.length()));
					}
					else if(wordLength>0){
						tokens.add(new Token(Kind.INTEGER_LITERAL,pos-word.length(),word.length(),line,posInLine-word.length()));
					}
					//posInLine-=1;
				}
				else throw new LexicalException("Not a valid input",pos);
			}
		}
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;

	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
