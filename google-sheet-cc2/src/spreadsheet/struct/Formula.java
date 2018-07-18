package spreadsheet.struct;

import java.util.ArrayList;
import java.util.Collections;

public class Formula {
	private ArrayList<StructDefine.R1C1Relative> InputSet;
	private String R1C1Formula;

	private enum State {
		START, COLUMN, CELL, ONECHAR, SOMECHARS, LEFT, END
	}

	public Formula() {
		InputSet = new ArrayList<StructDefine.R1C1Relative>();
	}

	public Formula(StructDefine.Position pos, String formula) {
		InputSet = new ArrayList<StructDefine.R1C1Relative>();
		R1C1Formula = convertToR1C1(pos, formula);
	}

	public String convertToR1C1(StructDefine.Position pos, String A1) {
		//System.err.println(StructDefine.Position.getA1(pos.GetRow(), pos.GetColumn()));
		//System.err.println(A1);
		A1 = A1.replaceAll(" ", "");
		if(A1.startsWith("+")) A1 = A1.substring(1);
		int row = pos.GetRow(), column = pos.GetColumn();
		//System.err.println(row+ ";" + column);
		String result = "";
		int length = A1.length(), index = 0;
		int lastCharIndex = -1;
		int lastInt = 0;
		State state = State.START;

		while (state != State.END) {
			char thisChar = '#';
			if (index == length) {
				state = State.END;
			} else {
				thisChar = A1.charAt(index++);
			}
			switch (state) {
			case START:
				if (thisChar >= 'A' && thisChar <= 'Z') {
					lastCharIndex = index-1;
					state = State.COLUMN;
				} else {
					result = result + thisChar;
				}
				break;
			case COLUMN:
				if (thisChar >= '0' && thisChar <= '9') {
					lastInt = thisChar - '0';
					//System.out.println(lastInt);
					state = State.CELL;
				} else if (thisChar >= 'A' && thisChar <= 'Z') {
					result = result + A1.charAt(lastCharIndex);
					lastCharIndex = index-1;
				} else {
					result = result + A1.charAt(lastCharIndex) + thisChar;
					lastCharIndex = -1;
					state = State.START;
				}
				break;
			case CELL:
				if (thisChar >= '0' && thisChar <= '9') {
					lastInt = 10 * lastInt + (thisChar - '0');
				} else {
					//System.err.println(A1.charAt(lastCharIndex) + ", " +A1.charAt(lastCharIndex-1));
					int columnInt = (A1.charAt(lastCharIndex) - 'A') - column;
					if(lastCharIndex > 0 && A1.charAt(lastCharIndex-1) >= 'A' && A1.charAt(lastCharIndex-1) <='Z') {
						result = result.substring(0, result.length()-1);
						columnInt += 26*(A1.charAt(lastCharIndex-1) - 'A' + 1);
					}

					int rowInt = lastInt - 1 - row;
					StructDefine.R1C1Relative thisPosition = new StructDefine.R1C1Relative(rowInt, columnInt);
					if(!InputSet.contains(thisPosition))
						InputSet.add(thisPosition);

					result = result + "<R[" + rowInt + "]";
					result = result + "C[" + columnInt + "]>";

					if (thisChar >= 'A' && thisChar <= 'Z') {
						lastCharIndex = index-1;
						state = State.COLUMN;
					} else {
						result = result + thisChar;
						lastCharIndex = -1;
						state = State.START;
					}

				}


				break;
			case END:
				if (lastCharIndex != -1) {
					int columnInt = (A1.charAt(lastCharIndex) - 'A') - column;
					if(lastCharIndex > 0 && A1.charAt(lastCharIndex-1) >= 'A' && A1.charAt(lastCharIndex-1) <='Z') {
						result = result.substring(0, result.length()-1);
						columnInt += 26*(A1.charAt(lastCharIndex-1) - 'A' + 1);
					}

					int rowInt = lastInt - 1 - row;
					StructDefine.R1C1Relative thisPosition = new StructDefine.R1C1Relative(rowInt, columnInt);
					if(!InputSet.contains(thisPosition))
						InputSet.add(thisPosition);

					result = result + "<R[" + rowInt + "]";
					result = result + "C[" + columnInt + "]>";
				}
				break;
			default:
				break;
			}
		}

		result = addFuncSign(result);
		//�������еģ�
		int sumStart = result.toLowerCase().indexOf("sum");
		while(sumStart != -1) {
			int sumEnd = result.indexOf("}", sumStart);
			String sum = result.substring(sumStart, sumEnd);
			String itemsString = sum.substring(4, sum.length()-1);
			String[] items = itemsString.split(",");
			for(String s : items) {
				String[] s1 = s.split(":");
				if(s1.length == 1) continue;
				//System.err.println(s1[0] + "," + s1[1]);
				StructDefine.R1C1Relative start = StructDefine.R1C1Relative.convertStringToR1C1Relative(s1[0]);
				StructDefine.R1C1Relative end = StructDefine.R1C1Relative.convertStringToR1C1Relative(s1[1]);
				for(int i = start.GetRow() ; i <= end.GetRow() ; i++)
					for(int j = start.GetColumn() ; j <=end.GetColumn() ; j++) {
						if((i == start.GetRow() && j == start.GetColumn()) || (i == end.GetRow() && j == end.GetColumn()))
							continue;
						InputSet.add(new StructDefine.R1C1Relative(i, j));
					}
			}
			sumStart = result.toLowerCase().indexOf("sum", sumEnd+1);
		}
		Collections.sort(InputSet);
		return result;
	}

	public String convertToA1(StructDefine.Position pos, String R1C1) throws Exception {
		String retString = "";
		int length = R1C1.length(), index = 0;
		State state = State.START;
		String unhandled = "";

		while(state != State.END) {
			char thisChar = '#';
			if (index == length) {
				state = State.END;
			} else {
				thisChar = R1C1.charAt(index++);
			}
			switch (state) {
			case START:
				if(thisChar == '<') {
					state = State.LEFT;
				}
				else if(thisChar != '{' && thisChar != '}'){
					retString += thisChar;
				}
				break;
			case LEFT:
				if(thisChar == '>') {
					StructDefine.Position position = transPos(unhandled, pos);
					retString += "" + (char)(position.GetColumn()+'A') + (position.GetRow()+1);
					state = State.START;
					unhandled = "";
				}
				else if(thisChar != '{' && thisChar != '}'){
					unhandled += thisChar;
				}
				break;
			default:
				break;
			}
		}
		return retString;
	}

	public ArrayList<StructDefine.R1C1Relative> GetInputSet() {
		return InputSet;
	}

	public String getR1C1Formula() {
		return R1C1Formula;
	}

	public String getA1Formula(StructDefine.Position pos) throws Exception {
		return convertToA1(pos, R1C1Formula);
	}

	private StructDefine.Position transPos(String pos, StructDefine.Position position) throws Exception {
		//��sheet��ȡ���ñ�����ֵ
		String[] temp = pos.split("\\[|]");
		int row = Integer.parseInt(temp[1]) + position.GetRow();
		int column = Integer.parseInt(temp[3]) + position.GetColumn();
		return new StructDefine.Position(row, column);
	}

	private String addFuncSign(String s) {
		String result = "";
		int length = s.length();
		int index = 0;
		int countOfLeft = 0;
		char thisChar = '#';
		String recordString = "";
		State state = State.START;

		while (state != State.END) {
			if (index == length) {
				state = State.END;
			} else {
				thisChar = s.charAt(index++);
				//System.out.println(thisChar);
			}
			switch (state) {
			case START:
				if (isLetter(thisChar)) {
					recordString += thisChar;
					state = State.ONECHAR;
				} else {
					result = result + thisChar;
					recordString = "";
				}
				break;
			case ONECHAR:
				if (isLetter(thisChar)) {
					recordString += thisChar;
					state = State.SOMECHARS;
				} else {
					result = result + recordString + thisChar;
					recordString = "";
					state = State.START;
				}
				break;
			case SOMECHARS:
				if (isLetter(thisChar)) {
					recordString += thisChar;
				}
				else if (thisChar == '(') {
					recordString += thisChar;
					state = State.LEFT;
					countOfLeft++;
				}
				else {
					result = result + recordString + thisChar;
					recordString = "";
					state = State.START;
				}
				break;
			case LEFT:
				recordString += thisChar;
				if(thisChar == '(') {
					countOfLeft++;	
				}
				if (thisChar == ')') {
					countOfLeft--;
					if(countOfLeft == 0) {
						result = result + "{" + recordString + "}";
						recordString = "";
						state = State.START;
					}
				}
				break;
			case END:
				if (recordString != "") {
					result = result + recordString;
					recordString = "";
				}
				break;
			default:
				break;
			}
		}
		return result;
	}

	private boolean isLetter(char c) {
		if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
			return true;
		else {
			return false;
		}
	}

}
