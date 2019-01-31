package com.hisun.regexp;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yzc
 * @since 2012/05/02
 */

public class HiRegExpHelp {
	private static PatternCompiler compiler = new Perl5Compiler();
	private static PatternMatcher matcher = new Perl5Matcher();

	/**
	 * 
	 * @param source
	 * @param reg
	 * @return
	 * @throws MalformedPatternException
	 */
	public static String getMatcherString(String source, String reg) throws MalformedPatternException {
		Pattern pattern = compiler.compile(reg);
		if (matcher.contains(source, pattern)) {
			MatchResult result = matcher.getMatch();
			return result.group(0);
		}
		return "";
	}

	/**
	 * 
	 * @param source
	 * @param regexp
	 * @param mask
	 * @return
	 * @throws MalformedPatternException
	 */
	public static boolean find(String source, String regexp, boolean mask) throws MalformedPatternException {
		Pattern pattern = null;
		if (mask)
			pattern = compiler.compile(regexp, Perl5Compiler.CASE_INSENSITIVE_MASK);
		else
			pattern = compiler.compile(regexp);
		if (matcher.contains(source, pattern)) {
			return true;
		} else
			return false;
	}

	public static List findNext(String source, String regexp, int mask) throws MalformedPatternException {
		PatternCompiler compiler = new Perl5Compiler();

		PatternMatcher matcher = new Perl5Matcher();

		ArrayList subString = new ArrayList();

		Pattern pattern = compiler.compile(regexp, mask);

		PatternMatcherInput input = new PatternMatcherInput(source);

		while (matcher.contains(input, pattern)) {
			MatchResult result = matcher.getMatch();
			for (int i = 1; i < result.length(); i++) {
				String matchStr = result.group(i);
				if (matchStr == null) {
					break;
				}
				subString.add(matchStr);
			}

		}

		return subString;
	}

	/**
	 * 
	 * @param source
	 * @param regexp
	 * @param mask
	 * @return
	 * @throws MalformedPatternException
	 */
	public static List getAllMatcher(String source, String regexp, boolean mask) throws MalformedPatternException {
		ArrayList<String> subString = new ArrayList<String>();
		Pattern pattern = null;
		if (mask)
			pattern = compiler.compile(regexp, Perl5Compiler.CASE_INSENSITIVE_MASK);
		else
			pattern = compiler.compile(regexp);

		PatternMatcherInput input = new PatternMatcherInput(source);

		while (matcher.contains(input, pattern)) {
			MatchResult result = matcher.getMatch();
			for (int i = 1; i < result.length(); i++) {
				String matchStr = result.group(i);
				if (matchStr == null)
					break;
				subString.add(matchStr);
			}
		}

		return subString;
	}

	/**
	 * 
	 * @param source
	 * @param regexp
	 * @param replace
	 * @param mask
	 * @return
	 * @throws MalformedPatternException
	 */
	public static String replace(String source, String regexp, String replace, int mask)
			throws MalformedPatternException {
		Pattern pattern = compiler.compile(regexp, mask);

		String result = Util.substitute(matcher, pattern, new Perl5Substitution(replace, 1), source,
				Util.SUBSTITUTE_ALL);
		return result;
	}

	/**
	 * 把敏感字符用*号代替,固定用法，不具有一般性
	 * 
	 * @param source
	 * @return
	 * @throws MalformedPatternException
	 */
	public static String desensitization(String source) throws MalformedPatternException {
		String reg_cn = "^\\s*[^\\x00-\\xff]+";
		String reg_mobile = "^1\\d{10}$";
		String reg_idno = "^\\d{15}\\d$|^\\d{17}[a-zA-Z0-9]{1}$";
		if (find(source, reg_idno, true)) {
			int repeat = source.length() - 6;
			source = source.substring(0, 6) + StringUtils.repeat("*", repeat) + source.substring(2 + repeat);
			System.out.println("id no=" + source);
		} else if (find(source, reg_mobile, true)) {
			int repeat = source.length() - 6;
			source = source.substring(0, 3) + StringUtils.repeat("*", repeat) + source.substring(2 + repeat);
			System.out.println("mobile=" + source);
		} else if (find(source, reg_cn, true)) {
			int repeat = source.length() - 1;
			if (repeat < 5) {
				source = StringUtils.repeat("*", repeat) + source.substring(repeat);
			} else {
				repeat = source.length() - 2;
				source = StringUtils.repeat("*", repeat) + source.substring(repeat);
			}

			System.out.println("chinese=" + source);
		} else {
			int repeat = source.length() - 3;
			if (repeat > 0) {
				source = source.substring(0, 2) + StringUtils.repeat("*", repeat) + source.substring(repeat + 2);
			} else {
				source = StringUtils.repeat("*", source.length());
			}
			System.out.println("charactor=" + source);
		}

		return source;
	}

	public static void main(String[] args) throws MalformedPatternException {
		String regexpForFontTag = "<\\s*font\\s+([^>]*\\s*)>";
		String regexpForFontAttr = "([a-z]+)\\s*=\\s*\"([^\"]+)\"";
		String font = "<font face=\"Aril,Serif\" size=\"+2\" color=\"red\">";

		PatternCompiler compiler = new Perl5Compiler();

		Pattern patternFontTag = compiler.compile(regexpForFontTag, Perl5Compiler.CASE_INSENSITIVE_MASK);

		Pattern patternFontAttr = compiler.compile(regexpForFontAttr, Perl5Compiler.CASE_INSENSITIVE_MASK);

		PatternMatcher matcher = new Perl5Matcher();

		if (matcher.contains(font, patternFontTag)) {
			MatchResult result = matcher.getMatch();

			String attribs = result.group(1);

			System.out.println(result.group(0));

			System.out.println(result.group(1));

			PatternMatcherInput input = new PatternMatcherInput(attribs);
			while (matcher.contains(input, patternFontAttr)) {
				result = matcher.getMatch();
				System.out.println(result.group(1) + ":" + result.group(2));
			}

		}

		String source = "<ROOT><A></A><MSG_TYP>N</MSG_TYp>fdsafds";
		String reg = "(<MSG_TYP>.</MSG_TYP>)";
		System.out.println(getMatcherString(source, reg));

	}
}
