package scu.fly.main;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SimpleExcelWrite {

	private int curRow = 0;
	public int getCurRow() {
		return curRow;
	}

	public void setCurRow(int curRow) {
		this.curRow = curRow;
	}

	public int getCurColumn() {
		return curColumn;
	}

	public void setCurColumn(int curColumn) {
		this.curColumn = curColumn;
	}

	private int curColumn = 0;
	private int sheetIndex = 0;
	private FileOutputStream os = null;
	private WritableWorkbook workbook = null;

	private ArrayList<Double> columnList = new ArrayList<Double>();
	private ArrayList<Double> rowList = new ArrayList<Double>();
	
	private WritableSheet sheet = null;

	public ArrayList<Double> getColumnList() {
		return columnList;
	}

	private static SimpleExcelWrite uniqueInstance = null;

	private SimpleExcelWrite() {
		try {
			os = new FileOutputStream("./result.xls");
			// 创建工作薄
			workbook = Workbook.createWorkbook(os);
			//创建表单
			sheet = workbook.createSheet("First Sheet", sheetIndex);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SimpleExcelWrite getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new SimpleExcelWrite();
		}
		return uniqueInstance;
	}

	public void setColumnList(ArrayList<Double> columnList) {
		this.columnList = columnList;
	}

	public ArrayList<Double> getRowList() {
		return rowList;
	}

	public void setRowList(ArrayList<Double> rowList) {
		this.rowList = rowList;
	}
	// 把创建的内容写入到输出流中
	public void writeRow(ArrayList<Double> list,boolean autoMove) throws WriteException, IOException {
		
		int column = curColumn;
		// 创建新的一页
		for (Double value : list) {

			writeRowData(value);
		}
		curColumn=column;
		if(autoMove) moveDown();

	}
	// 把创建的内容写入到输出流中
	public void writeColumn(ArrayList<Double> list,boolean autoMove)  throws WriteException, IOException {
		
		int row = curRow;
		// 创建新的一页
		for (Double value : list) {

			writeColumnData(value);
		}
		curRow=row;
		if(autoMove) moveRight();
	}
	public void moveRight()
	{
		curColumn++;
	}
	public void moveLeft()
	{
		curColumn--;
	}
	public void moveUp()
	{
		curRow--;
	}
	public void moveDown()
	{
		curRow++;
	}
	public void setCurPosition(int row,int column)
	{
		curColumn = column;
		curRow = row;
	}
	public void writeColumnData(Double value) throws WriteException, IOException {

		Number cell = new Number(curColumn, curRow++, value);
		sheet.addCell(cell);
	}
	public void writeRowData(Double value) throws WriteException, IOException {

		Number cell = new Number(curColumn++, curRow, value);
		sheet.addCell(cell);
	}
	
	public void closeExcel() throws IOException, WriteException {

		workbook.write();
		workbook.close();
		os.close();
	}
	
	public void createExcel(OutputStream os) throws WriteException, IOException {
		// 创建工作薄
		WritableWorkbook workbook = Workbook.createWorkbook(os);
		// 创建新的一页
		WritableSheet sheet = workbook.createSheet("First Sheet", 0);

		for (Double value : columnList) {
			Label label = new Label(curRow, curColumn++, value.toString());
			sheet.addCell(label);
		}

		// 把创建的内容写入到输出流中，并关闭输出流
		workbook.write();
		workbook.close();
		os.close();
	}

}