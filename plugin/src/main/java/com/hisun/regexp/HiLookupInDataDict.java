package com.hisun.regexp;

import com.hisun.constants.HiConstants;
import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiContext;
import com.hisun.sm.HiStringManager;
import com.hisun.util.HiICSProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.*;
import java.util.Hashtable;

/**
 * 
 * @author yzc
 * @since 2012/05/02
 */
public class HiLookupInDataDict {
	private Logger logger = (Logger) HiContext.getCurrentContext().getProperty(HiConstants.SERVERLOG);

	private final HiStringManager sm = HiStringManager.getManager();

	private static HiLookupInDataDict instance = null;

	private FileReader reader = null;

	private Hashtable dictTable = new Hashtable();

	private final String fileName = System.getProperty(HiConstants.ICS_HOME) + File.separator + "conf" + File.separator
			+ "dict.dat";

	/**
	 * 
	 * 
	 */
	private HiLookupInDataDict() {
		logger = (Logger) HiContext.getCurrentContext().getProperty(HiConstants.SERVERLOG);
	}

	/**
	 * 
	 * 
	 * @return
	 * @throws HiException
	 */
	synchronized public static HiLookupInDataDict getInstnace() {
		if (instance == null) {
			instance = new HiLookupInDataDict();
			instance.loadDict();
		}

		return instance;
	}

	/**
	 *
	 * @return
	 */
	public Hashtable loadDict() {
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
			logger.error(HiMessageCode.ERR_FILE_NOT_EXIST, e.getStackTrace());
		}
		BufferedReader br = new BufferedReader(reader);
		String dictLine = null;
		dictTable.clear();
		try {
			while ((dictLine = br.readLine()) != null) {
				if (!StringUtils.isAlphanumeric(dictLine.substring(0, 1))) // ������ַ�����ĸ������
					continue;
				int pos = dictLine.indexOf(":");// ��ð�ŷָ�
				String dictName = dictLine.substring(0, pos);
				String regexp = dictLine.substring(pos + 1);
				dictTable.put(dictName, regexp);
			}
			br.close();
			reader.close();

		} catch (IOException e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
			logger.error(e.getStackTrace());
		}

		if (logger.isDebugEnabled())
			logger.debug("load data dictionay completed.");
		return dictTable;
	}

	/**
	 * 
	 * 
	 * @param idx
	 * @return
	 */
	public boolean isInDict(String idx) {
		return (dictTable.containsKey(idx));
	}

	/**
	 *
	 * 
	 * @param source
	 *            --
	 * @param regexp
	 *            --
	 * @param mask
	 *            --
	 * @return
	 */
	public boolean isMatchRegular(String source, String dictName, boolean mask) {

		String regexp = (String) dictTable.get(dictName);

		if (StringUtils.isEmpty(regexp)) {
			logger.error("regexp is empty.");
			return false;
		}

		if (logger.isDebugEnabled())
			logger.debug("regexp", regexp);
		System.out.println("regexp=" + regexp);

		try {
			return HiRegExpHelp.find(source, regexp, mask);
		} catch (MalformedPatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Usage:hivalidate validate_str dict_name");
			return;
		}

		String source = args[0];
		String dictName = args[1];

		System.out.println(HiICSProperty.getConfDir());
		HiContext.getRootContext().setProperty(HiConstants.SERVERLOG, HiLog.getLogger("server.trc"));
		HiContext.pushCurrentContext(HiContext.getRootContext());
		HiLookupInDataDict dataDict = HiLookupInDataDict.getInstnace();
		System.out.println(dataDict.fileName);
		if (dataDict.isInDict(dictName)) {
			System.out.println(dictName + " is in the dicionary.");
			if (dataDict.isMatchRegular(source, dictName, false)) {
				System.out.print("[ok]");
				System.out.println(dictName + ":" + source);
			} else {
				System.out.print("[fail]");
				System.out.println(dictName + ":" + source);
			}
		} else {
			System.out.println(dictName + "is not in the dicionary.");
		}

	}
}

class dict {
	private String idxName = null;

	private String regexp = null;

	public String getIdxName() {
		return idxName;
	}

	public void setIdxName(String idxName) {
		this.idxName = idxName;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

}
