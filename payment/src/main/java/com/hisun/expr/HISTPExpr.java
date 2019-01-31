package com.hisun.expr;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiMessageContext;

public class HISTPExpr {

	/**
	 * <p>
	 * 校验时间是否合法 ,返回0-true合法，1-false非法 表達式開發，只能返回字符串類型
	 * </p>
	 */
	public static String CHECKTIME(Object ctx, String[] args)
			throws HiException {
		if (args.length < 2)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
					"timeStr,fmt");
		String timeStr = args[0];
		String fmt = args[1];
		HiMessageContext dd = (HiMessageContext) ctx;
		Logger log = HiLog.getLogger(dd.getCurrentMsg());
		log.info(timeStr);
		log.info(fmt);
		if (fmt.equalsIgnoreCase("HHMM")) {
			if (timeStr.length() != 4) {
				return "1";
			}
			int hour = Integer.parseInt(timeStr.substring(0, 2));
			int minute = Integer.parseInt(timeStr.substring(2));
			if ((hour >= 0) && (hour <= 23) && (minute >= 0) && (minute <= 59)) {
				return "0";
			} else {
				return "1";
			}
		}
		return "1";
	}

	/**
	 * <p>
	 * 返回字串在字符串中最后一次出现的位置，如果不存在则返回-1
	 * </p>
	 */
	public static String GETLASTPOS(Object ctx, String[] args)
			throws HiException {
		if (args.length < 2)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
					"timeStr,fmt");
		String sourceStr = args[0];
		String subStr = args[1];
		int pos = sourceStr.lastIndexOf(subStr);

		return String.valueOf(pos);
	}

	/**
	 * 輸入ASCALL碼
	 * 
	 * @param ctx
	 * @param args
	 * @return
	 * @throws HiException
	 */
	public static String CHAR(Object ctx, String[] args) throws HiException {
		if (args.length < 1)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "CHAR");
		char c = (char) NumberUtils.toInt(StringUtils.trim(args[0]));
		return new String(new char[] { c });
	}

	/**
	 * <p>
	 * 正則表達式驗證，返回0-符合 -1-不符合
	 * </p>
	 */
	public static String CHECK_REGEXP(Object ctx, String[] args)
			throws HiException {
		if (args.length < 2)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
					"regStr,checkStr");
		String regStr = args[0];
		String checkStr = args[1];
		HiMessageContext dd = (HiMessageContext) ctx;
		/*
		 * Logger log=HiLog.getLogger(dd.getCurrentMsg()); log.info(regStr);
		 * log.info(checkStr);
		 */
		Pattern p = Pattern.compile(regStr);
		Matcher m = p.matcher(checkStr);
		if (m.matches())
			return "0";
		else
			return "-1";
	}

	/**
	 * <p>
	 * 正則表達式驗證，返回: 0-成功 1-長度必須在6到10個字符 2-密碼必須以小寫字母、大寫字母或下劃線開頭
	 * 3-密碼中必須包含至少一個小、大寫字母和一個特殊字符
	 * (`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？_)
	 * </p>
	 */
	public static String CHECK_STPPWD(Object ctx, String[] args)
			throws HiException {
		if (args.length < 1)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "pwdStr");
		String pwdStr = args[0];
		HiMessageContext dd = (HiMessageContext) ctx;
		/*
		 * Logger log=HiLog.getLogger(dd.getCurrentMsg()); log.info(regStr);
		 * log.info(checkStr);
		 */
		// 密碼長度6-10位
		if (pwdStr.length() < 6 || pwdStr.length() > 10)
			return "1";
		// 密碼必須以小寫字母、大寫字母或下劃線開頭
		String result = HISTPExpr
				.CHECK_REGEXP(
						null,
						new String[] {
								"[a-zA-Z0-9_][a-zA-Z0-9`~!@#$%^*()+-=|\\[\\]\\{\\}:;',.>/\\?]{5,9}",
								pwdStr });
		if (Integer.parseInt(result) != 0)
			return "2";
		// 密碼中必須包含至少一個小、大寫字母和一個特殊字符(`~!@#$%^*()+-=|[]{}:;',.>/\?)
		Pattern p1 = Pattern.compile("[a-z]");
		Pattern p2 = Pattern.compile("[A-Z]");
		Pattern p3 = Pattern
				.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？_]");
		Matcher m1 = p1.matcher(pwdStr);
		Matcher m2 = p2.matcher(pwdStr);
		Matcher m3 = p3.matcher(pwdStr);
		// System.out.println(m1.find()+" "+m2.find()+" "+m3.find());
		if (m1.find() && m2.find() && m3.find()) {
			return "0";
		} else {
			return "3";
		}
	}

	/**
	 * <p>
	 * 数字型的金额转为英文表达，如下： 279268.30=TWO HUNDRED SEVENTY-NINE THOUSAND AND TWO
	 * HUNDRED SIXTY-EIGHT AND THIRTY CENTS ONLY 返回值说明: 英文字符-成功 1-失败
	 * </p>
	 */
	public static String AMT2Eng(Object ctx, String[] args) throws HiException {
		if (args.length < 1)
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "AMT2Eng");
		String AMTStr = args[0];
		String EngStr = AMT2EngUtil.parse(AMTStr);

		return EngStr;
	}

	public static String subStrByWord(Object ctx, String[] args)
			throws HiException {
		if (args.length < 3)
			throw new HiException("215110", "subStrByWord");
		int beginIndex = NumberUtils.toInt(StringUtils.trim(args[1])) - 1;
		int endpoint = 0;
		byte[] bytes = args[0].getBytes();
		if (beginIndex < 0) {
			beginIndex = 0;
		}
		if (beginIndex > bytes.length) {
			beginIndex = bytes.length;
		}
		int length = NumberUtils.toInt(StringUtils.trim(args[2]));
		if (length < 0) {
			length = 0;
		}
		if (beginIndex + length > bytes.length)
			length = bytes.length - beginIndex;
		endpoint = beginIndex + length;
		do {
			beginIndex++;
			length--;
			if ((beginIndex == 0) || (bytes[(beginIndex - 1)] == 32))
				break;
		} while (beginIndex < bytes.length);

		for (; (endpoint < bytes.length) && (bytes[(endpoint - 1)] != 32)
				&& (bytes[endpoint] != 32); length++)
			endpoint++;

		return new String(bytes, beginIndex, length);
	}

	/**
	 * <p>
	 * 传入两个参数arg[0]需要去除的符号；arg[1]需要处理的字符床
	 * </p>
	 * 
	 * @throws HiException
	 */
	public static String STRINGFILTER(Object ctx, String[] args)
			throws PatternSyntaxException, HiException {
		// regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		// String regEx="[-|?|(|:|)|.|'|+|,|/|\r|\n| |\t|#|@|{|]";
		if (args.length != 2) {
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
					"StringFilter");
		}
		if (args[0].equals("")) {
			return "";
		}
		Pattern p = Pattern.compile("[" + args[0] + "]");
		Matcher m = p.matcher(args[1]);
		return m.replaceAll("");
	}

	/**
	 * <p>
	 * 传入四个参数argg[0]当前日期
	 * 		   args[1]货币代码
	 * 		   args[2]标志 1 表示自然日;2表示工作日;3表示休假日
	 * 		   args[3]天数，负数也支持向前查找 
	 * </p>
	 * 
	 * @throws HiException
	 */
	public static String COUNTDAY(Object oCtx, String[] args) throws HiException,
			SQLException {
		if (args.length != 4) {
			throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
					"COUNTDAY");
		}
		HiMessageContext ctx = (HiMessageContext)oCtx;
		String currentDate = args[0];
		String moneyType = args[1];
		int day = Integer.parseInt(args[3]);
		int type = Integer.parseInt(args[2]);
		int currentYear = Integer.parseInt(currentDate.substring(0, 4));
		int currentMonth = Integer.parseInt(currentDate.substring(4, 6));
		int currentDay = Integer.parseInt(currentDate.substring(6, 8));
		switch (type) {
		case (1): {
			if (day > 0) {
				String natureResult = countNatureDay(currentYear, currentMonth,
						currentDay, day, moneyType, ctx);
				return natureResult;
			} else {
				String natureResult = countNatureDayN(currentYear,
						currentMonth, currentDay, day, moneyType, ctx);
				return natureResult;
			}
		}
		case (2): {
			if (day > 0) {
				String workResult = countWorkDay(currentYear, currentMonth,
						currentDay, day, moneyType, ctx);
				return workResult;
			} else {
				String workResult = countWorkDayN(currentYear, currentMonth,
						currentDay, day, moneyType, ctx);
				return workResult;
			}

		}
		case (3): {
			if (day > 0) {
				String relaxResult = countRelaxDay(currentYear, currentMonth,
						currentDay, day, moneyType, ctx);
				return relaxResult;
			} else {
				String relaxResult = countRelaxDayN(currentYear, currentMonth,
						currentDay, day, moneyType, ctx);
				return relaxResult;
			}

		}
		default: {
			return "";
		}
		}
	}

	public static String countNatureDayN(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		String result = "";
		HiYearToArr hiYearTemp = new HiYearToArr(year, month, moneyType,
				countDay, ctx);
		String[] monthTableTemp = hiYearTemp.getMonthArr();
		int count = day - monthTableTemp[0].length();
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			for (int i = 0; i < monthTable.length; i++) {
				for (int j = monthTable[i].length() - 1; j >= 0; j--) {
					count++;
					if (count == -countDay + 1) {
						System.out.println(j);
						if (i > hiYear.getTop()) {
							month = -13 + i + month;
							year--;
						} else {
							month = month - i;
						}
						j++;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}

						return result;
					}
				}
			}
			year--;
		}
	}

	public static String countNatureDay(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		int count = -day;
		String result = "";
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			int test = 0;
			for (int i = 0; i < monthTable.length; i++) {
				test = test + monthTable[i].length();
				System.out.println(monthTable[i]);
				for (int j = 0; j < monthTable[i].length(); j++) {
					count++;
					if (count == countDay) {
						System.out.println("--" + hiYear.getTop());
						if (i >= hiYear.getTop()) {
							year++;
							month = -12 + i + month;
							;
						} else {
							month = month + i;
						}
						j = j + 1;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}
						return result;
					}
				}
			}
			System.out.println("Test" + test);
			year++;
		}
	}

	public static String countWorkDay(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		String result = "";
		HiYearToArr hiYearTemp = new HiYearToArr(year, month, moneyType,
				countDay, ctx);
		String[] monthTableTemp = hiYearTemp.getMonthArr();
		int count = -monthTableTemp[0].substring(0, day).replace("H", "")
				.length();
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			for (int i = 0; i < monthTable.length; i++) {
				for (int j = 0; j < monthTable[i].length(); j++) {
					if (monthTable[i].charAt(j) == 'W') {
						count++;
					}
					if (count == countDay) {
						if (i >= hiYear.getTop()) {
							year++;
							month = -12 + i + month;
							;
						} else {
							month = month + i;
						}
						j = j + 1;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}
						return result;
					}
				}
			}
			year++;
		}
	}

	public static String countWorkDayN(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		String result = "";
		HiYearToArr hiYearTemp = new HiYearToArr(year, month, moneyType,
				countDay, ctx);
		String[] monthTableTemp = hiYearTemp.getMonthArr();
		int count = -(monthTableTemp[0].substring(day,
				monthTableTemp[0].length()).replace("H", "").length());
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			System.out.println(monthTable[0].length());
			System.out.println(count);
			for (int i = 0; i < monthTable.length; i++) {
				for (int j = monthTable[i].length() - 1; j >= 0; j--) {
					if (monthTable[i].charAt(j) == 'W') {
						count++;
					}
					if (count == -countDay) {
						System.out.println(j);
						if (i > hiYear.getTop()) {
							month = -13 + i + month;
							year--;
						} else {
							month = month - i;
						}
						j++;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}

						return result;
					}
				}
			}
			year--;
		}
	}

	public static String countRelaxDay(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		String result = "";
		HiYearToArr hiYearTemp = new HiYearToArr(year, month, moneyType,
				countDay, ctx);
		String[] monthTableTemp = hiYearTemp.getMonthArr();
		int count = -monthTableTemp[0].substring(0, day).replace("W", "")
				.length();
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			for (int i = 0; i < monthTable.length; i++) {
				for (int j = 0; j < monthTable[i].length(); j++) {
					if (monthTable[i].charAt(j) == 'H') {
						count++;
					}
					if (count == countDay) {
						if (i >= hiYear.getTop()) {
							year++;
							month = -12 + i + month;
							;
						} else {
							month = month + i;
						}
						j = j + 1;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}
						return result;
					}
				}
			}
			year++;
		}
	}

	public static String countRelaxDayN(int year, int month, int day,
			int countDay, String moneyType, HiMessageContext ctx) throws HiException,
			SQLException {
		String result = "";
		HiYearToArr hiYearTemp = new HiYearToArr(year, month, moneyType,
				countDay, ctx);
		String[] monthTableTemp = hiYearTemp.getMonthArr();
		int count = -(monthTableTemp[0].substring(day,monthTableTemp[0].length()).replace("W", "").length());
		while (true) {
			HiYearToArr hiYear = new HiYearToArr(year, month, moneyType,
					countDay, ctx);
			String[] monthTable = hiYear.getMonthArr();
			System.out.println(monthTable[0].length());
			System.out.println(count);
			for (int i = 0; i < monthTable.length; i++) {
				for (int j = monthTable[i].length()-1; j >= 0; j--) {
					if (monthTable[i].charAt(j) == 'H') {
						count++;
					}
					if (count == -countDay) {
						System.out.println(j);
						if (i > hiYear.getTop()) {
							month = -13+i+month;
							year--;
						} else {
							month = month - i;
						}
						j++;
						if (month < 10) {
							result = year + "0" + month;
						} else {
							result = year + "" + month;
						}
						if (j < 10) {
							result = result + "0" + j;
						} else {
							result = result + "" + j;
						}

						return result;
					}
				}
			}
			year--;
		}
	}
	/**
	 * @param args
	 * @throws HiException
	 */
	public static void main(String[] args) throws HiException {
		// TODO Auto-generated method stub
		// HISTPExpr.checkTime(null, null);
		// String result = HISTPExpr.CHECK_STPPWD(null, new String[]{"Asf3A4"});
		// System.out.println(result);

		String[] num = new String[] { "279268.30" };

		for (int i = 0; i < num.length; i++) {
			System.out.println(num[i] + "=" + HISTPExpr.AMT2Eng(null, num));
		}

	}

}
