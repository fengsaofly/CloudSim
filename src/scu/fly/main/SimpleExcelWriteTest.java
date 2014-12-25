package scu.fly.main;

import java.io.IOException;
import java.util.ArrayList;

import jxl.write.WriteException;

public class SimpleExcelWriteTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		SimpleExcelWrite excelWrite = SimpleExcelWrite.getInstance();
//		ArrayList<Double> arrayList = new ArrayList<>();
//		ArrayList<Double> xList = new ArrayList<>();
//		for (int i = 0; i < 10; i++) {
//			arrayList.add(new Double((double)i*0.6));
//			xList.add(new Double(i+1));
//		}
		
		
		
//		try {
			for(int i=0;i<11;i++)
			{
				double x = i*0.1*121.1+95.895;
				System.out.print(x+" ");
				
			}
//			excelWrite.writeRow(arrayList,true);
//			excelWrite.writeRow(xList,true);
			
////			excelWrite.closeExcel();
//		} catch (WriteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
