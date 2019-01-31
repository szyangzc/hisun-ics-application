package com.hisun.expr;


import com.hisun.database.HiDataBaseUtil;
import com.hisun.database.HiResultSet;
import com.hisun.exception.HiException;
import com.hisun.message.HiMessageContext;

import java.sql.SQLException;

public class HiYearToArr {
	private String[] monthArr = new String[12];
	private int year;
	private int month;
	private String moneyType;
	private int countDay;
	private int top;

	public int getTop() {
		return top;
	}

	public int getCountDay() {
		return countDay;
	}

	public void setCountDay(int countDay) {
		this.countDay = countDay;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getMoneyType() {
		return moneyType;
	}

	public void setMoneyType(String moneyType) {
		this.moneyType = moneyType;
	}

	public String[] getMonthArr() {
		return monthArr;
	}

	public String[] setMonthArr( HiMessageContext ctx, int countDay) throws HiException,
			SQLException {
		HiDataBaseUtil dbUtil = (HiDataBaseUtil) ctx.getDataBaseUtil();
		String[] findMonthSqlArr;
		if (countDay < 0) {
			findMonthSqlArr = findMonthSqlMin(this.month, this.year);
		} else {
			findMonthSqlArr = findMonthSqlMax(this.month, this.year);
		}
		if (findMonthSqlArr[1].equals("")) {
			HiResultSet rs =dbUtil.execQuerySQL(findMonthSqlArr[0]);
			for (int i = 0; i < rs.getRecord(0).size(); i++) {
				this.monthArr[i] = rs.getRecord(0).getValue(i).trim();
			}
			this.top = rs.getRecord(0).size();
			return monthArr;
		} else {
			HiResultSet rs =dbUtil.execQuerySQL(findMonthSqlArr[0]);
			for (int i = 0; i < rs.getRecord(0).size(); i++) {
				System.out.println(rs.getRecord(0).getValue(i));
				this.monthArr[i] = rs.getRecord(0).getValue(i).trim();
				this.top = rs.getRecord(0).size();

			}
			HiResultSet rsMax =dbUtil.execQuerySQL(findMonthSqlArr[1]);
			for (int i = 0; i < rsMax.getRecord(0).size(); i++) {
				System.out.println(rsMax.getRecord(0).size());
				System.out.println(rs.getRecord(0).size());
				this.monthArr[rs.getRecord(0).size() + i] = rsMax.getRecord(0)
						.getValue(i).trim();
			}
			return this.monthArr;
		}
	}

	public HiYearToArr() {
	}

	public HiYearToArr(int year, int month, String moneyType, int countDay,
			HiMessageContext ctx) throws HiException, SQLException {
		this.year = year;
		this.month = month;
		this.moneyType = moneyType;
		this.countDay = countDay;
		this.monthArr = setMonthArr(ctx, countDay);

	}

	public String[] findMonthSqlMax(int month, int year) {
		String[] findMonthSqlArr = new String[2];
		String monthSqlMin = "";
		String monthSqlMax = "";
		for (int i = month; i <= 12; i++) {
			if (i == 12) {
				monthSqlMin = monthSqlMin + "PCAL_MONTH_" + i;
			} else {
				monthSqlMin = monthSqlMin + "PCAL_MONTH_" + i + ",";
			}
		}
		findMonthSqlArr[0] = "select " + monthSqlMin
				+ "  from STPTPCAL where PCAL_YEAR=" + year + " and PCAL_CCY='"
				+ moneyType + "'";
		for (int i = 1; i < month; i++) {
			if (i == month - 1) {
				monthSqlMax = monthSqlMax + "PCAL_MONTH_" + i;
			} else {
				monthSqlMax = monthSqlMax + "PCAL_MONTH_" + i + ",";
			}
		}
		year++;
		if (monthSqlMax.equals("")) {
			findMonthSqlArr[1] = "";
		} else {
			findMonthSqlArr[1] = "select " + monthSqlMax
					+ "  from STPTPCAL where PCAL_YEAR=" + year
					+ " and PCAL_CCY='" + moneyType + "'";
		}
		return findMonthSqlArr;
	}

	public String[] findMonthSqlMin(int month, int year) {
		String[] findMonthSqlArr = new String[2];
		String monthSqlMin = "";
		String monthSqlMax = "";
		for (int i = month; i > 0; i--) {
			if (i == 1) {
				monthSqlMin = monthSqlMin + "PCAL_MONTH_" + i;
			} else {
				monthSqlMin = monthSqlMin + "PCAL_MONTH_" + i + ",";
			}
		}
		findMonthSqlArr[0] = "select " + monthSqlMin
				+ "  from STPTPCAL where PCAL_YEAR=" + year + " and PCAL_CCY='"
				+ moneyType + "'";
		for (int i = 12; i > month; i--) {
			if (i == month + 1) {
				monthSqlMax = monthSqlMax + "PCAL_MONTH_" + i;
			} else {
				monthSqlMax = monthSqlMax + "PCAL_MONTH_" + i + ",";
			}
		}
		year--;
		if (monthSqlMax.equals("")) {
			findMonthSqlArr[1] = "";
		} else {
			findMonthSqlArr[1] = "select " + monthSqlMax
					+ "  from STPTPCAL where PCAL_YEAR=" + year
					+ " and PCAL_CCY='" + moneyType + "'";
		}
		return findMonthSqlArr;
	}
}
