package com.hisun.atc;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class ExcelParse {
	public static String[] parseExcel_Head(File excelFile,int titleLine){
		try {
			
			InputStream is = new FileInputStream(excelFile);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet sheet = rwb.getSheet(0);
			Cell [] headCells = sheet.getRow(titleLine-1);
			String [] head_Str = new String[headCells.length];
			for(int i=0;i<headCells.length;i++){
				if(headCells[i].getContents().trim().length()!=0){
					head_Str[i] = headCells[i].getContents().trim();
				}else{
					head_Str[i] = "_null";
				}
			}
			/*鼓励GC 回收一下内存*/
			headCells = null;
			sheet = null;
			rwb = null;
			is = null;
			return head_Str;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[][] parseExcel_Cont(File excelFile,int titleLine){
		try {
			String [] heads = ExcelParse.parseExcel_Head(excelFile,titleLine);
			InputStream is = new FileInputStream(excelFile);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet sheet = rwb.getSheet(0);
			int rows = sheet.getRows();
			int columns = sheet.getColumns();
			Cell [] rowCells = null;
			String[][] cont_Str = new String[rows-titleLine][columns];
			for(int i=titleLine;i<rows;i++){
				rowCells = sheet.getRow(i);
				for(int j=0;j<columns;j++){
					if((j<heads.length)&&!(heads[j].equals(new String("_null")))){
						//System.out.println(ExcelParse.parseExcel_Head(excelFile)[j]);
						try{
							cont_Str[i-titleLine][j] = rowCells[j].getContents();
						}catch(ArrayIndexOutOfBoundsException e){
							cont_Str[i-titleLine][j] = "";
						}
					}else{
						cont_Str[i-titleLine][j] = "_null";
					}
					
				}
			}
			/*鼓励GC 回收一下内存*/
			rowCells = null;
			sheet = null;
			rwb = null;
			is = null;
			excelFile = null;
			return cont_Str;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String [] args){
		File excelFile = new File("F:\\tmpfordev\\IBTMTD1.xls");
		String [] heads = ExcelParse.parseExcel_Head(excelFile,1);
		for(int i=0;i<heads.length;i++){
			System.out.println(heads[i]);
		}
		
		String rtn = new Integer(123).toString();
		System.out.println("****************");
		String [][] conts = ExcelParse.parseExcel_Cont(excelFile,1);
		for(int i=0;i<conts.length;i++){
			for(int j=0;j<conts[i].length;j++){
				System.out.print(conts[i][j]+"|")	;
			}
			System.out.println(" ");
		}
		
		if(heads==null||conts==null){
			System.out.println("error");
		}
	}
}
