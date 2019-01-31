package com.hisun.app.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiXmlUtils {
	/**
	 * 把xml节点转成大写
	 * 
	 * @param xmlString
	 * @return
	 */
	public static String xmlNodeNameToUpercase(String xmlString) {
		Pattern pattern = Pattern.compile("<.+?>");
		StringBuilder res = new StringBuilder();

		int lastIdx = 0;
		Matcher matchr = pattern.matcher(xmlString);

		while (matchr.find()) {
			String str = matchr.group();
			res.append(xmlString.substring(lastIdx, matchr.start()));
			res.append(str.toUpperCase());
			lastIdx = matchr.end();
		}

		res.append(xmlString.substring(lastIdx));

		System.out.println(res.toString());

		return res.toString();

	}

	public static String modifyXmlProlog(String xmlString, String charset) {
		Pattern pattern = Pattern.compile("<\\?.+?\\?>");
		StringBuilder res = new StringBuilder();

		int lastIdx = 0;
		Matcher matchr = pattern.matcher(xmlString);

		String s = "<?xml version='1.0' encoding=" + "'" + charset + "'?>";
		while (matchr.find()) {
			String str = matchr.group();
			res.append(xmlString.substring(lastIdx, matchr.start()));

			res.append(s);
			lastIdx = matchr.end();
		}

		res.append(xmlString.substring(lastIdx));
		if (lastIdx == 0)
			res.insert(0, s);
		System.out.println(res.toString());

		return res.toString();

	}
}
