import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class CppToAssembly {

	List<String> code = new ArrayList<String>();
	String[] operators = new String[] {"+", "-", "*", "/"};

	public void generateAssembly(String file) throws IOException{
		List<String> declarations = new ArrayList<String>();
		List<String> codeList = new ArrayList<String>();
		
		String dataLine, codeLine;
		dataLine = codeLine = "";
		int msgCtr=0;
		
		// Reads the cpp program
		String line;
		InputStream fis = new FileInputStream("./"+file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			code.add(line);
		}
		
		boolean elser, body, whiler, doer, forer, isMessage;
		elser = body = whiler = doer = forer = isMessage = false;
		int elseIfCtr=1;
		for(int i=0; i<code.size(); i++){
			String x = code.get(i);
			if((x.contains("int") || x.contains("char") || x.contains("string")) && 
					!x.contains("main")){
				declarations.add(x);
				dataLine += initializeValues(x);
			}
			
			if(x.contains("main")){
				body = true;
			}
			
			// display messages
			if(x.contains("cout") && x.contains("\"")){
				msgCtr++;
				codeList = writeToCodeSegmentA(x, msgCtr);
				isMessage = true;
			}
			else{
				isMessage = false;
			}
			
			// display variables, explicit numbers and characters
			if(x.contains("cout") && !x.contains("\"")){
				codeLine += writeToCodeSegmentB(x, declarations);
			}
			
			// assignment operations
			if(x.contains("=") && !isMessage && (!x.contains("while") && !x.contains("for"))){
				codeLine += move(x);
			}
			
			// increment/decrement
			if((x.contains("++") || x.contains("--")) && !x.contains("for")){
				codeLine += incDec(x);
			}
			
			if(x.contains("if") && !isMessage && !x.contains("string")){
				if(x.contains("else")){
					codeLine += "jmp end_if\n";
					codeLine += "else_if"+elseIfCtr+":\n";
					elseIfCtr++;
				}
				codeLine += writeIfCode(x, elseIfCtr);
			}
			
			if(x.contains("else") && !x.contains("if") && !isMessage && !x.contains("string")){
				codeLine += "jmp end_if\n";
				codeLine += "else_if"+elseIfCtr+":\n";
				elser = true;
			}
			
			if(x.contains("}") && elser && !isMessage){
				codeLine += "end_if:\n";
				elser = false;
			}
			
			if(x.contains("while") && !isMessage && !x.contains("string")){	// while or do-while(end)
				codeLine += writeDoOrWhileCode(x);
				whiler = true;
			}
			
			if(x.contains("}") && whiler && !doer && !isMessage){
				codeLine += "jmp whilelabel\n";
				codeLine += "endwhile:\n";
				whiler = false;
			}
			
			if(x.contains("do") && !isMessage && !x.contains("string")){
				codeLine += "do:\n";
				doer = true;
			}
			
			if(x.contains("for") && !isMessage && !x.contains("string")){
				codeLine += writeForLoopCode(x);
				forer = true;
			}
			
			if(x.contains("}") && forer && !isMessage){
				codeLine += "jmp forlabel\n";
				codeLine += "endfor:\n";
				forer = false;
			}
			
			if(!codeList.isEmpty()){
				dataLine += codeList.get(0);
				codeLine += codeList.get(1);
				codeList.clear();
			}
		}
				
		String finalCode = writeAssembly(dataLine, codeLine);
		
		br.close();
		br = null;
		fis = null;
		
		System.out.println("Assembling " +file+ "...\n");
		System.out.println("The program has successfully assembled your cpp file!\n");
		Scanner x = new Scanner(System.in);
		System.out.println("Enter your desired name of the asm file: ");
		String newFile = x.nextLine();
		
		// Write To File
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
		try{
			outputStream = new PrintWriter(new FileOutputStream("./"+newFile));
			outputStream.println(finalCode);
			outputStream.close();
			System.out.println("File Created!");
			System.out.println("Thank you for using Disassembler!");
		}
		catch(FileNotFoundException e){
			System.out.println("Error opening the file out.txt");
		}
	}
	
	private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	private String initializeValues(String str){
		String dataLine="";
		String[] tempA = new String[5];
		int j=0;
		StringTokenizer st = new StringTokenizer(str, ",");
		while(st.hasMoreTokens()){
			tempA[j] = st.nextToken();
			j++;
		}
		for(int i=0; i<j; i++){
			dataLine += tokenizeEquality(tempA[i]);
		}	
		return dataLine;
	}
	
	private String tokenizeEquality(String str){
		String dataLine="";

		String[] temp = new String[2];
		temp[1] = "?";
		
		int j=0;
		StringTokenizer te = new StringTokenizer(str, "=");
		while(te.hasMoreTokens()){
			temp[j] = te.nextToken();
			j++;
		}
		temp[0] = temp[0].replaceAll("int", "");
		temp[0] = temp[0].replaceAll("char", "");
		
		// string x = "Hello"
		if(temp[0].contains("string")){
			temp[1] = temp[1].replaceAll("\"", "'");
			temp[1] = temp[1].trim();
			temp[1] += ", '$'";
			temp[0] = temp[0].replaceAll("string", "");
		}
		temp[0] = temp[0].replaceAll(";", "");
		temp[0] = temp[0].trim();
		temp[1] = temp[1].replaceAll(";", "");
		if(operator(temp[1])==null)
			dataLine += temp[0] + " db " + temp[1]+"\n";
		else{
			if(operator(temp[1])=="*")
				dataLine += temp[0] + " dw ?\n";
			else
				dataLine += temp[0] + " db ?\n";
		}
		return dataLine;
	}
	
	private String writeToDataSegment(String str){
		String dataLine="";
		String temp[] = new String[2];
		temp[1] = "?";
				
		StringTokenizer st = new StringTokenizer(str, "=");
		int i=0;
		while(st.hasMoreTokens()){
			temp[i] = st.nextToken();
			i++;
		}
		
		String dt = "db";
		/*
		 * Safe Inputs
		 * NOT int x = 5, y=3;	NOT int x = y+z;
		 */
		if(!temp[1].contains(",") && operator(temp[1])==null){
			if(temp[0].contains("int")){
				dt = "db";
			}
			temp[0] = temp[0].replaceAll("int", "");
			temp[0] = temp[0].replaceAll("char", "");
			temp[1] = temp[1].replaceAll(";", "");
			if(temp[0].contains("string")){
				temp[1] = temp[1].replaceAll("\"", "'");
				temp[1] = temp[1].trim(); // mark n.i.
				temp[1] += ", '$'";
				temp[0] = temp[0].replaceAll("string", "");
			}
			temp[0] = temp[0].replaceAll(";", "");
			temp[0] = temp[0].trim(); // mark n.i.
			dataLine += temp[0] + " " + dt + " " + temp[1];
			dataLine += "\n";
		}
		
		return dataLine;
	}
	
	private List<String> writeToCodeSegmentA(String str, int msgCtr){
		String codeLine, dataLine;
		codeLine = dataLine = "";
		String temp[] = new String[3];
		StringTokenizer st = new StringTokenizer(str, "\"");
		int j=0;
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		String message=temp[1];
		if(temp[1].substring(0,2).equals("\\n")){
			codeLine += newLine();
			message = temp[1].substring(2, temp[1].length());
		}
		
		codeLine += "lea dx, message" + msgCtr + "\n";
		codeLine += "mov ah, 09h\n";
		codeLine += "int 21h\n";
		
		if((temp[1].substring(temp[1].length()-2, temp[1].length()).equals("\\n"))){
			codeLine += newLine();
			message = temp[1].substring(0, temp[1].length()-2);
		}
		if(str.contains("endl"))
			codeLine += newLine();
		
		dataLine += "message" + msgCtr + " db '";
		String quoteVar = "', 39,'";
		String[] quoTemp = new String[10];
		int qc=0;
		// special case '
		if(message.contains("'")){
			StringTokenizer sq = new StringTokenizer(message, "'");
			while(sq.hasMoreTokens()){
				quoTemp[qc] = sq.nextToken();
				qc++;
			}
			for(int k=0; k<qc; k++){
				dataLine += quoTemp[k];
				if(k!=qc-1)
					dataLine += quoteVar;
			}
			dataLine += "', '$'\n";
		}
		else
			dataLine += message + "', '$'\n";
		
		List<String> tempList = new ArrayList<String>();
		tempList.add(dataLine);
		tempList.add(codeLine);
		
		return tempList;
	}
	
	private String writeToCodeSegmentB(String str, List<String> declarations){
		String codeLine="";
		String temp[] = new String[3];
		int identifier = 0;
		StringTokenizer st = new StringTokenizer(str, "<<");
		int j=0;
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		temp[1] = temp[1].replaceAll(";", "");
		temp[1] = temp[1].trim();
		
		if(temp[1].contains("'"))
			identifier=2;
		else if(isInteger(temp[1]))
			identifier=1;
		else{
			for(String d : declarations){
				if(d.contains(temp[1]) && d.contains("int"))
					identifier=1;
				else if(d.contains(temp[1]) && d.contains("char"))
					identifier=2;
				else if(d.contains(temp[1]) && d.contains("string"))
					identifier=3;
			}
		}
		
		switch(identifier){
			/*case 1: codeLine += "mov al, "+temp[1]+"\n";
					codeLine += "add al, 30h\n";
					codeLine += "mov dl, al\n";
					codeLine += "mov ah, 02h\n";
					codeLine += "int 21h\n";
					break;*/
			case 1: codeLine += "mov al, "+temp[1]+"\n";
					codeLine += "mov ah, 0\n";
					codeLine += "call displayNum\n";
					break;
			case 2: codeLine += "mov dl, "+temp[1]+"\n";
					codeLine += "mov ah, 02h\n";
					codeLine += "int 21h\n";
					break;
			case 3: codeLine += "lea dx, "+temp[1]+"\n";
					codeLine += "mov ah, 09h\n";
					codeLine += "int 21h\n";
					break;
		}
		if(str.contains("endl") || str.contains("\\n"))
			codeLine += newLine();
		
		return codeLine;
	}
	
	private String move(String str){
		// di na kasali yung tipong int a=3, char x='c', string x="asa"
		String codeLine="";
		
		String[] temp = new String[3];
		int j=0;
		StringTokenizer st = new StringTokenizer(str, "=");
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		temp[1] = temp[1].replaceAll(";", "");
		temp[1] = temp[1].trim();
		
		boolean mover = true;
		if((temp[0].contains("int") || temp[0].contains("char") || temp[0].contains("string")) 
			&& (isInteger(temp[1]) || temp[1].contains("'") || temp[1].contains("\""))){
				mover = false;
		}
		
		temp[0] = temp[0].replaceAll("int", "");
		temp[0] = temp[0].trim();
		
		if(mover){
			// Case 1: int z = x + y or z = x + y
			if(operator(temp[1]) != null){
				StringTokenizer op = new StringTokenizer(temp[1], operator(temp[1]));
				String[] tempA = new String[2];
				int k=0;
				while(op.hasMoreTokens()){
					tempA[k] = op.nextToken();
					k++;
				}
				tempA[0] = tempA[0].trim();
				tempA[1] = tempA[1].trim();
				
				codeLine += "mov al, "+tempA[0]+"\n";
				codeLine += "mov bl, "+tempA[1]+"\n";
				
				if(operator(temp[1]).equals("+")){
					codeLine += "add al, bl\n"; 	
					codeLine += "mov "+temp[0]+", al\n";
				}
				else if(operator(temp[1]).equals("-")){
					codeLine += "sub al, bl\n";
					codeLine += "mov "+temp[0]+", al\n";
				}
				else if(operator(temp[1]).equals("*")){
					codeLine += "mul bl\n";
					codeLine += "mov "+temp[0]+", ax\n";
				}
				
			}
			
			// Case 2	x = 5;
			else if(str.contains("=") && !str.contains("int") && (operator(str)==null)){
				codeLine += "mov " +temp[0]+ ", " +temp[1]+ "\n";
			}	
			
			// Case 3	x+=5
			else if(operator(temp[0]) != null){ // x += 5
				codeLine += "mov al, "+temp[1]+"\n";
				if(temp[0].contains("+") && !temp[0].contains("++")){
					temp[0] = temp[0].replaceAll("[+]", "");
					temp[0] = temp[0].trim();
					codeLine += "add " +temp[0]+ ", al\n";
				}
				else if(temp[0].contains("-") && !temp[0].contains("--")){
					temp[0] = temp[0].replaceAll("[-]", "");
					temp[0] = temp[0].trim();
					codeLine += "sub " +temp[0]+ ", al\n";
				}
			}
		}
		
		return codeLine;
	}
	
	private String operator(String str){
		for(int i=0; i<operators.length; i++){
			if(str.contains(operators[i]))
				return operators[i]; 
		}
		return null;
	}
	
	private String incDec(String str){
		String codeLine="";
		String[] temp = new String[3];
		int j=0;
		StringTokenizer st = new StringTokenizer(str, "++/--");
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		temp[0] = temp[0].trim();
		if(str.contains("++"))
			codeLine = "inc " +temp[0]+ "\n";
		else if(str.contains("--"))
			codeLine = "dec " +temp[0]+ "\n";
		
		return codeLine;
	}
	
	private String writeIfCode(String str, int elseIfCtr){
		String x = "";
		String converse = "";
		String symbol = "";
		String temp[] = new String[3];
			
		if(str.contains(">") && !str.contains("=")){
			converse = "jle";
			symbol = ">";
		}
		else if(str.contains("<") && !str.contains("=")){
			converse = "jge";
			symbol = "<";
		}
		else if(str.contains("==")){
			converse = "jne";
			symbol = "==";
		}
		else if(str.contains(">=")){
			converse = "jl";
			symbol = ">=";
		}
		else if(str.contains("<=")){
			converse = "jg";
			symbol = "<=";
		}
		else if(str.contains("!=")){
			converse = "je";
			symbol = "!=";
		}
			
		int j=0;
		StringTokenizer st = new StringTokenizer(str, symbol);
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		temp[0] = temp[0].replaceAll("if", "");
		temp[0] = temp[0].replaceAll("else", "");
		temp[0] = temp[0].replaceAll("[()]","");
		temp[0] = temp[0].trim();
		temp[1] = temp[1].replaceAll("[()]", "");
		temp[1] = temp[1].replaceAll("[{}]", " ");
		temp[1] = temp[1].trim();
		
		x += "cmp " +temp[0]+ ", " +temp[1]+ "\n";
		x += converse + " else_if"+elseIfCtr+"\n";
		
		return x;
	}
	
	private String writeDoOrWhileCode(String str){
		String converse = "", condition = "";
		String x = "";
		String symbol = "";
		String temp[] = new String[3];
		if(str.contains("while")){
			if(str.contains(">") && !str.contains("=")){
				converse = "jle";
				condition = "jg";
				symbol = ">";
			}
			else if(str.contains("<") && !str.contains("=")){
				converse = "jge";
				condition = "jl";
				symbol = "<";
			}
			else if(str.contains("==")){
				converse = "jne";
				condition = "je";
				symbol = "==";
			}
			else if(str.contains(">=")){
				converse = "jl";
				condition = "jge";
				symbol = ">=";
			}
			else if(str.contains("<=")){
				converse = "jg";
				condition = "jle";
				symbol = "<=";
			}
			
			int j=0;
			StringTokenizer st = new StringTokenizer(str, symbol);
			while(st.hasMoreTokens()){
				temp[j] = st.nextToken();
				j++;
			}
			temp[0] = temp[0].replaceAll("while", "");
			temp[0] = temp[0].replaceAll("[()]","");
			temp[0] = temp[0].replaceAll("[{}]","");
			temp[0] = temp[0].trim();
			temp[1] = temp[1].replaceAll("[()]", "");
			temp[1] = temp[1].replaceAll("[{}]", " ");
			temp[1] = temp[1].trim();
			
			if(str.contains("while") && !str.contains(";"))	// while
				x += "whilelabel:\n";
			
			x += "cmp " +temp[0]+ ", " +temp[1]+ "\n";
			
			if(str.contains("while") && !str.contains(";"))	// while
				x += converse + " endwhile\n";
			else if(str.contains("while") && str.contains(";"))	// do-while
				x += condition + " do\n";
		}
		
		return x;
	}
	
	private String writeForLoopCode(String str){
		str = str.replaceAll("for", "");
		str = str.replaceAll("[()]", "");
		str = str.replaceAll("[{}]", "");
		str = str.replaceAll(" ", "");
		str = str.trim();
		
		String x = "";
		
		String[] temp = new String[3];
		int j=0;
		StringTokenizer st = new StringTokenizer(str, ";");
		while(st.hasMoreTokens()){
			temp[j] = st.nextToken();
			j++;
		}
		
		// Tokenize first part (i=0)
		j=0;
		StringTokenizer f1 = new StringTokenizer(temp[0], "=");
		while(f1.hasMoreTokens()){
			if(j==0){
				x += "mov " +f1.nextToken()+ ", ";
			}
			else{
				x += f1.nextToken() + "\n";
			}
			j++;
		}
		
		x += "forlabel:\n";
		
		// Tokenize second part (i<5)
		String converse, symbol;
		converse = symbol = "";
		if(temp[1].contains(">") && !temp[1].contains("=")){
			converse = "jle";
			symbol = ">";
		}
		else if(temp[1].contains("<") && !temp[1].contains("=")){
			converse = "jge";
			symbol = "<";
		}
		else if(temp[1].contains("==")){
			converse = "jne";
			symbol = "==";
		}
		else if(temp[1].contains(">=")){
			converse = "jl";
			symbol = ">=";
		}
		else if(temp[1].contains("<=")){
			converse = "jg";
			symbol = "<=";
		}
		j=0;
		StringTokenizer f2 = new StringTokenizer(temp[1], symbol);
		while(f2.hasMoreTokens()){
			if(j==0)
				x += "cmp " +f2.nextToken()+ ", ";
			else
				x += f2.nextToken() + "\n";
			j++;
		}
		x += converse + " endfor\n";
		
		// Tokenize third part (i++)
		if(temp[2].contains("++")){
			temp[2] = temp[2].replaceAll("[++]", "");
			temp[2] = temp[2].trim();
			x += "inc " +temp[2]+ "\n";
		}
		else if(temp[2].contains("--")){
			temp[2] = temp[2].replaceAll("[--]", "");
			temp[2] = temp[2].trim();
			x += "dec " +temp[2]+ "\n";
		}
		
		return x;
			
	}
		
	private String newLine(){
		return "mov dl, 0ah\n" +	
				"mov ah, 02h\n" + 
				"int 21h\n";
	}
	
	private String displayNum(){
		return "displayNum proc\n" +				
			   "mov bx, 10\n" +			
			   "mov dx, 0000h\n" +			
			   "mov cx, 0000h\n" +		
			   "loop1:\n" +
			   "mov dx, 0000h\n" +			
			   "div bx\n" +				
			   "push dx\n" +				
			   "inc cx\n" +			
			   "cmp ax, 0\n" +			
			   "jne loop1\n" +				
			   "loop2:\n" +		
			   "pop dx\n" +				
			   "add dx, 30h\n" +			
			   "mov ah, 02h\n" +				
			   "int 21h\n" +
			   "loop loop2\n" +			
			   "ret\n" +			
			   "displayNum endp\n";
	}
	
	private String writeAssembly(String dataLine, String codeLine){
		String writer = ".model small\n";
		writer += ".data\n";
		writer += dataLine;	
		writer += ".stack 100h\n"
				+ ".code\n";
		
		writer += "main proc\n"
				+ "mov ax, @data\n"
				+ "mov ds,ax\n";
		writer += codeLine;
		
		writer += "mov ax, 4c00h\n"
				+ "int 21h\n";
		writer += displayNum();
		writer += "main endp\n"
				+ "end main\n";
		
		return writer;
	}
	
	
	public static void main(String[] args){
		Scanner s = new Scanner(System.in);
		System.out.println("Enter the cpp file to convert: ");
		String filename = s.nextLine();
		CppToAssembly ca = new CppToAssembly();
		
		try {
			ca.generateAssembly(filename);
			
		} catch (IOException e) {
			System.err.println("File Not Found");
		}
	}
	
}
