package spreadsheet.parser;

import java.io.IOException;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get;
import com.google.api.services.sheets.v4.model.ValueRange;

import spreadsheet.struct.StructDefine;
import spreadsheet.struct.StructDefine.Cell;

public class SheetReader {
	private String spreadsheetId;
	private String sheetName;
	private int rowCount = 0, colCount = 0;
	private StructDefine.Cell[][] cells;
	
	public SheetReader(Sheets service, String spreadsheetId, String sheetTitle) throws IOException {
		this.sheetName = sheetTitle;
		this.spreadsheetId = spreadsheetId;
		Get request = service.spreadsheets().values()
				.get(this.spreadsheetId, this.sheetName);
		request.setValueRenderOption("FORMULA");		
		ValueRange response = request.execute();
		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			System.out.println("No data found.");
		} else {
			for (List<Object> row : values ) {
			    if(row != null) {
			    		if(row.size()>colCount) colCount = row.size();
			    }
			    rowCount++;
			}
			int rCount = 0;
			for (List<Object> row : values ) {
			    if(row != null) {
			    		for(int cCount = 0; cCount<row.size(); cCount++) 
			    			this.cells[rCount][cCount] = (row.get(cCount) == null) ? new Cell() : new Cell(row.get(cCount));
			    }
			    rCount++;
			}
		}
		
		
//		this.rowCount = sheet.getLastRowNum()+1;
//
//		for (int i = 0; i < this.rowCount; i++) {
//			Row row = sheet.getRow(i);
//			if(row == null) continue;
//			if(this.columnCount < row.getLastCellNum())
//				this.columnCount = row.getLastCellNum();
//		}
//		
//		this.cells = new StructDefine.Cell[rowCount][columnCount];
//		for(int i = 0 ; i < rowCount ; i++)
//			for(int j = 0; j < columnCount ; j++){
//				this.cells[i][j] = new StructDefine.Cell();
//			}
//			
//		for (int i = 0; i < this.rowCount; i++) {
//			Row row = sheet.getRow(i);
//			if(row == null) continue;
//
//			for (int j = 0; j < row.getLastCellNum(); j++) {
//				Cell cell = row.getCell(j);
//				if (cell == null) continue;
//				this.cells[i][j].setCellType(cell.getCellType()); 
//				switch (cell.getCellType()) {
//					case Cell.CELL_TYPE_NUMERIC:
//						this.cells[i][j].setValue(cell.getNumericCellValue() + "");
//						this.cells[i][j].setValueType(0);
//						break;
//					case Cell.CELL_TYPE_STRING:
//						this.cells[i][j].setValue(cell.getStringCellValue());
//						if(cell.getStringCellValue().length() > 0)
//							this.cells[i][j].setValueType(1);
//						break;
//					case Cell.CELL_TYPE_BOOLEAN:
//						this.cells[i][j].setValue(cell.getBooleanCellValue() ? "true" : "false");
//						this.cells[i][j].setValueType(2);
//						break;
//					case Cell.CELL_TYPE_FORMULA:
//						this.cells[i][j].setFormula(cell.getCellFormula());
//						try{
//							cell.getNumericCellValue();
//							this.cells[i][j].setValueType(0);
//							this.cells[i][j].setValue(cell.getNumericCellValue() + "");
//						} catch(IllegalStateException e1){
//							try{
//								cell.getStringCellValue();
//								this.cells[i][j].setValueType(1);
//							} catch(IllegalStateException e2){
//								try{
//									cell.getBooleanCellValue();
//									this.cells[i][j].setValueType(2);
//								} catch(IllegalStateException e3){
//									
//								}
//							}
//						}
//						break;
//					case Cell.CELL_TYPE_BLANK:
//						break;
//					case Cell.CELL_TYPE_ERROR:
//						break;
//				} 
//			}
//		}
	}

	public String getSheetName() {
		return this.sheetName;
	}
	
	public int getRowCount() {
		return this.rowCount;
	}
	
	public int getColCount() {
		return this.colCount;
	}
	
	public StructDefine.Cell[][] getCells() {
		return this.cells;
	}
	
	public void setCellValueAt(double d, int i, int j) {
		cells[i][j].setValue(d+"");
	}
	
	public void setCellFormulaAt(String formula, int i, int j) {
		cells[i][j].setFormula(formula);
	}
	
	public double getCellValueAt(StructDefine.Position pos) {
		if(cells[pos.GetRow()][pos.GetColumn()].getCellType() == 1)
			return Double.parseDouble(cells[pos.GetRow()][pos.GetColumn()].getValue());
		else
			return 0;
	}
	
}
