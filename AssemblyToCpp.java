import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;


public class AssemblyToCpp {
		private static Scanner s;
		List<String> dataType = new ArrayList<String>();//var_name db "Hello!", '$' --> String var_name = "hello";
		List<String> varName = new ArrayList<String>();		
		List<String> varValue = new ArrayList<String>();
		String[] reservedAscii = new String[]{"10", "39", "0ah"};
		String[] valueAscii = new String[]{"\\n", "'", "\\n"};
		
		/**
		 * 
		 * @param filename
		 * @return List of code in Assembly per line
		 */
		public List<String> getFromFile(String filename)throws FileNotFoundException, IOException{
			List<String> codeLines = new ArrayList<String>();
			String line;
			InputStream fis = new FileInputStream("./" + filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				codeLines.add(line);
			}
			return codeLines;
		}
		
		public List<String> getDataLines(List<String> code){
			boolean reachedDataSegment = false;
			//boolean doneOnDataSegment = false;
			List<String> dataSegmentCodes = new ArrayList<String>();
	
			for(int i=0; i<code.size(); i++){
				String x = code.get(i);
				int y = x.length();
				if((x.contains(".code") || x.contains(".stack"))){
					break;
				}
				if(reachedDataSegment){
					dataSegmentCodes.add(x);
				}
				if(x.contains(".data")){		//reachedDataSegment
					reachedDataSegment = true;
				}
			}// should collected all data segments here.
			//Now extract and convert variables...

			for(int i=0; i <dataSegmentCodes.size(); i++){
				//TEST IF this is a string
				String temp_String = dataSegmentCodes.get(i);
				//System.out.println(temp_String);
				if((temp_String.contains("\"") || temp_String.contains("'")) && temp_String.contains("$")){
					String toTokenizeBySpaces = temp_String;
					StringTokenizer tokenizerOfString = new StringTokenizer(toTokenizeBySpaces);
					String tempVarName = tokenizerOfString.nextToken();
					dataType.add("string");
					varName.add(tempVarName);
					String tokenizeByQuotes = temp_String;					
					
					StringTokenizer tokenizerOfQuotes = new StringTokenizer(tokenizeByQuotes, "'");
					String dataValue = "";
					while(tokenizerOfQuotes.hasMoreTokens()){
						String tmp = tokenizerOfQuotes.nextToken();
						boolean cont = false;
						if(tmp.equals("db") || tmp.equals("dw")) continue;
						if(tmp.equals("$")) break;
						for(int p = 0; p < reservedAscii.length; p++){
							if(tmp.contains(reservedAscii[p])){
								dataValue += valueAscii[p];
								cont = true;
								break;
							}
						}
						if(cont) continue;
						else dataValue += tmp;
					}
					StringTokenizer lastTokenize = new StringTokenizer(dataValue);
					lastTokenize.nextToken();
					lastTokenize.nextToken();
					String finalDataVar = "";
					while(lastTokenize.hasMoreTokens()){
						finalDataVar += lastTokenize.nextToken() + " ";  
					}
					varValue.add(finalDataVar);
				}
				else if(temp_String.trim().isEmpty()){
					continue;
				}
				else {
					String s2 = temp_String;
					StringTokenizer tokenizeVarName_2 = new StringTokenizer(s2);
					dataType.add("int");
					varName.add(tokenizeVarName_2.nextToken());		//var name
					tokenizeVarName_2.nextToken();			// db
					String tempVarName = tokenizeVarName_2.nextToken();	//value
					if(tempVarName.equalsIgnoreCase("?"))
						varValue.add("0");					// for var_name db ?
					else varValue.add(tempVarName);
					
				}
			}
			return dataSegmentCodes;
		}
		
		public List<String> getCodeSegment(List<String> code){
			boolean reachedCodeSegment = false;
			
			List<String> convertedCodeSegment = new ArrayList<String>();

			List<String> codeSegment = new ArrayList<String>();
			for(int i=0; i< code.size(); i++){
				String x = code.get(i);
				int y = x.length();
				if((x.contains("end") && x.contains("main"))){
					break;
				}
				if(reachedCodeSegment){
					codeSegment.add(x);
				}
				if(x.contains(".code")){		//reachedDataSegment
					reachedCodeSegment = true;
				}
			}
			boolean thereIsElse = false;
			boolean startOfLoop = false;
			boolean isDoWhile = false;
			boolean isWhileLoop = false;
			boolean onlyIf = true;
			String stash = "";
			String stashValue = "";
			boolean firstIsCmp = false;
			String endIfLabel = "";
			String endWhileLabel = "";
			String labelElse = "";
			//substring contains lea ...
			for(int j = 0; j < codeSegment.size(); j++){
				String s = codeSegment.get(j);
				String tempNext = "";
				String toAdd = "";
				if(j != codeSegment.size() - 1)
					tempNext = codeSegment.get(j+1);
				
				if(s.contains("@data") || s.contains("ds")) continue;
				
				if(s.contains("mov") &&! s.contains("4c00h")){
					String t = s;
					StringTokenizer tok = new StringTokenizer(t, "\t ,");
					if(tok.nextToken().equals("mov")){
						String tmp = tok.nextToken(); 	//mov ah, 39 //stash = ah, stashValue = 39
						String tmp_2 = tok.nextToken();
						if(isRegister(tmp)){			//stash = ah, stashValue = x
							stash = tmp;
							stashValue = tmp_2;
						}
						else if(isADataVar(tmp)){
							toAdd = tmp + " = " + stashValue;
							convertedCodeSegment.add(toAdd);
						}
					}
				}
				if(s.contains("lea") && s.contains("dx")){
					StringTokenizer tokens = new StringTokenizer(s, "\t, ");
					String coutVar = "";
					tokens.nextToken();
					tokens.nextToken();
					coutVar = tokens.nextToken();
					if(coutVar.equals("0ah") || coutVar.equals("0a")){
						coutVar = "endl";
					}
					toAdd = "cout << " + coutVar;
					convertedCodeSegment.add(toAdd);
				}// end if of LEA DX
				
				else if(s.contains("add")){
					String q = s;
					StringTokenizer t = new StringTokenizer(q, "\t ,");
					t.nextToken();
					String var = t.nextToken();
					String addend = t.nextToken();
					if(addend.equals("'0'"))continue;
					if(isRegister(var)){
						//toAdd = stashValue + " = " + stashValue + " + " + addend;
						stashValue = stashValue + " + " + addend;
						//convertedCodeSegment.add(toAdd);
					}
					else {
						//toAdd = var + " = " + var + " + " + addend;
						stashValue = var + " + " + stashValue;
						//convertedCodeSegment.add(toAdd);
					}
				}
				
				else if(s.contains("sub")){
					String q = s;
					StringTokenizer t = new StringTokenizer(q, "\t ,");
					t.nextToken();
					String var = t.nextToken();
					String sub = t.nextToken();
					if(sub.equals("'0'"))continue;
					if(isRegister(var)){
						//toAdd = stashValue + " = " + stashValue + " - " + sub;
						stashValue = stashValue + " - " + sub;
						//convertedCodeSegment.add(toAdd);
					}
					else {
						stashValue = var + " - " + stashValue;
					};
				}
						
				else if(s.contains("mov") && !s.contains(" ah")){
					String coutVar = "";
					if(s.contains("dl") && (tempNext.contains("02h") || tempNext.contains("02"))){
						StringTokenizer tokens = new StringTokenizer(s, "\t, ");
						while(tokens.hasMoreTokens()){
							if(tokens.nextToken().equalsIgnoreCase("dl"))
									break;
						}//skip parts
						coutVar = tokens.nextToken();
						if(coutVar.equals("0ah") || coutVar.equals("0a")){
							coutVar = "endl";
						}
						toAdd = "cout << " + coutVar;
						convertedCodeSegment.add(toAdd);
						j++;
					}else if(s.contains("offset")){
						StringTokenizer tokens = new StringTokenizer(s, "\t, ");
						while(tokens.hasMoreTokens()){
							if(tokens.nextToken().equalsIgnoreCase("offset"))
									break;
						}//skip parts
						for(int k = 0; k < varName.size(); k++){
							if(tokens.nextToken().equalsIgnoreCase(varName.get(k))){
								coutVar = varName.get(k);
								break;
							}//end if
						}// end for
						coutVar = tokens.nextToken();
						toAdd = "cout << " + coutVar;
						convertedCodeSegment.add(toAdd);
						j++;
					}
				}//end else if for printing characters
				else if(s.contains("mov") && s.contains("ah") && s.contains("1")){
					String cinVar = "";
					int moved  = 0;
					while(j < codeSegment.size() - 1){
						moved++;
						String tmp = codeSegment.get(j+moved);		//forecast
						if(tmp.contains("mov") && tmp.contains("al"))	//got it!
							break;
					}//end while
					String locationOfVarAssignment = codeSegment.get(j+moved);
					StringTokenizer tokens = new StringTokenizer(locationOfVarAssignment, "\t, ");
					tokens.nextToken();
					boolean out = false;
					while(tokens.hasMoreTokens()){
						String str = tokens.nextToken();
						for(int k = 0; k < varName.size(); k++){
							if(str.equalsIgnoreCase(varName.get(k))){
								cinVar = varName.get(k);
								out = true;
								break;
							}//end if
						}// end for
						if(out)break;
					}// end while
					toAdd = "cin << " + cinVar; 
					convertedCodeSegment.add(toAdd);
				}//end else if for CIN
				
				else if(s.contains("cmp")){
					if(!startOfLoop){
						firstIsCmp = true;
					}
					String compare = "";
					String compare_2 = "";
					StringTokenizer tokens = new StringTokenizer(s, "\t, ");
					if(tokens.countTokens() == 3){	//cmp 
						tokens.nextToken();
					}
					String tmp = tokens.nextToken();
					String tmp2 = tokens.nextToken();
					for(int k = 0; k < varName.size(); k++){
						if(tmp.equalsIgnoreCase(varName.get(k))){
							compare = tmp;
							compare_2 = tmp2;
							break;
						}
						else if(tmp2.equalsIgnoreCase(varName.get(k))){
							compare = tmp2;
							compare_2 = tmp;
							break;
						}
					}
					String condition = "";
					String tmp_next = tempNext;
					StringTokenizer tmpToken = new StringTokenizer(tmp_next);
					String jmpCond = tmpToken.nextToken();
					labelElse = tmpToken.nextToken();

					switch(jmpCond){
						case "jl": condition = ">";break;
						case "jg": condition = "<";break;
						case "je": condition = "!=";break;
						case "jne": condition = "=";break;
						case "jle": condition = ">";break;
						case "jge": condition = "<";break;
					}
					if(startOfLoop && isDoWhile){
						condition = reverseCondition(condition);
						toAdd = "}while (" + compare + " " + condition + " " + compare_2 + ")" ;
						startOfLoop = false;
					}
					else if(startOfLoop && isWhileLoop){
						toAdd = "while (" + compare + " " + condition + " " + compare_2 + ") {" ;
					}
					else if(!startOfLoop){
						toAdd = "if (" + compare + " " + condition + " " + compare_2 + "){" ;
						for(int l = j; l < codeSegment.size(); l++){
							String tmpstr = codeSegment.get(l);
							if(tmpstr.contains("jmp")){
								thereIsElse = true;
								StringTokenizer tok = new StringTokenizer(tmpstr);
								tok.nextToken();
								endIfLabel = tok.nextToken();
								onlyIf = false;
								break;
							}
						}
					}
					convertedCodeSegment.add(toAdd);
					j++; 
				}
				
				else if(s.contains(":") && !startOfLoop && !firstIsCmp){
					startOfLoop = true;
					int jmpCount = 0;
					for(int l = j; l < codeSegment.size(); l++){
						String tmpstr = codeSegment.get(l);
						if(isJMP(tmpstr)){
							jmpCount++;
							if(tmpstr.contains("jmp")){
								StringTokenizer token = new StringTokenizer(tmpstr);
								token.nextToken();
								endWhileLabel = token.nextToken();
							}
						}		
					}
					if(jmpCount == 1){
						toAdd = "do {";
						convertedCodeSegment.add(toAdd);
						isDoWhile = true;
					}											
					else if(jmpCount == 2){
						isWhileLoop = true;
					}		
				}
				
				else if(thereIsElse && s.contains(endIfLabel + ":") && firstIsCmp){
					toAdd = "}";
					convertedCodeSegment.add(toAdd);
				}
				
				else if(s.contains(labelElse + ":") && thereIsElse && firstIsCmp){
					toAdd = "else {";
					convertedCodeSegment.add(toAdd);
				}
				
				else if(s.contains(endIfLabel) && s.contains("jmp") && firstIsCmp){
					toAdd = "}";
					thereIsElse = true;
					convertedCodeSegment.add(toAdd);
				}
				
				else if(s.contains(endWhileLabel) && s.contains("jmp") && isWhileLoop && startOfLoop ){
					toAdd = "}";
					startOfLoop = false;
					convertedCodeSegment.add(toAdd);
				}
				
				else if (s.contains("inc")){
					String p = s;
					StringTokenizer inc = new StringTokenizer(p, "\t ");
					inc.nextToken();
					String toInc = inc.nextToken();
					toAdd = toInc + "++";
					convertedCodeSegment.add(toAdd);
				}
				else if (s.contains("dec")){
					String p = s;
					StringTokenizer inc = new StringTokenizer(p, "\t ");
					inc.nextToken();
					String toInc = inc.nextToken();
					toAdd = toInc + "--";
					convertedCodeSegment.add(toAdd);
				}
				
				else if(s.contains("4c00h")){
					toAdd = "return 0";
					convertedCodeSegment.add(toAdd);
				}
				
			}//end for
			
			Stack<String> stack = new Stack<String>();
			for(int k = 0; k < convertedCodeSegment.size(); k++){
				if(convertedCodeSegment.get(k).contains("{"))
					stack.push("{");
				if(convertedCodeSegment.get(k).endsWith("}"))
					stack.pop();
			}
			
			while(!stack.isEmpty()){
				stack.pop();
				convertedCodeSegment.add("}");
			}
			
			return convertedCodeSegment;
		}
		
		private boolean isJMP(String k){
			if(k.contains("jl") || k.contains("jg") ||k.contains("jge") || k.contains("jle") || k.contains("je") || k.contains("jne") 
					|| k.contains("jmp"))
				return true;
			else return false;
		}
		
		private boolean isRegister(String k){
			if(k.equals("ah") || k.equals("al") || k.equals("bh") || k.equals("bl"))
				return true;
			else return false;
		}
		
		private boolean isADataVar(String k){
			for(int i = 0; i < varName.size(); i++){
				if(k.equals(varName.get(i))){
					return true;
				}
			}
			return false;
		}
		
		private String reverseCondition(String prim){
			switch(prim){
				case "<": return ">";
				case "<=": return ">";
				case "=>": return "<";
				case ">": return "<";
				case "=": return "!=";
				case "!=": return "=";
			}
			return "";
		}
		
		private void writeToFile(List<String> data, List<String> body, String fileout) throws FileNotFoundException{
			PrintWriter outputStream = new PrintWriter(new FileOutputStream("./" + fileout));
			
			outputStream.println("#include <iostream>");
			outputStream.println();
			outputStream.println("using namespace std;");
			outputStream.println();
			for(int i = 0; i < dataType.size(); i++){
				outputStream.print(dataType.get(i) + " " + varName.get(i) + " = ");
				if(dataType.get(i).equals("string")){
					outputStream.println("\"" + varValue.get(i) + "\"" + ";");					 
				}
				 else{
					 outputStream.println(varValue.get(i) + ";");
				 }
			}
			outputStream.println();
			outputStream.println("int main() {");
			
			for(int i = 0; i < body.size(); i++){
				if(body.get(i).endsWith("{") || body.get(i).endsWith("}"))
					outputStream.println("\t" + body.get(i));
				else outputStream.println("\t" + body.get(i) + ";");
			}
			outputStream.println("}");
			outputStream.flush();
			outputStream.close();
		}
		
		public void generateCpp(String filename, String fileout) throws FileNotFoundException, IOException {
			List<String> codeLines = new ArrayList<String>();
			List<String> dataLines = new ArrayList<String>();
			List<String> codeSegmentLines = new ArrayList<String>();
			codeLines = getFromFile(filename);
			dataLines = getDataLines(codeLines);
			codeSegmentLines = getCodeSegment(codeLines);
			writeToFile(dataLines,codeSegmentLines, fileout);
		}
		
		public static void main(String[] args){
			Scanner s = new Scanner(System.in);
			System.out.println("Enter the assembly file to convert: ");
			String filename = s.nextLine();
			System.out.println("Enter the desired name of file: ");
			String fileout = s.nextLine();
			AssemblyToCpp conv = new AssemblyToCpp();
			
			try {
				conv.generateCpp(filename, fileout);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	
}
