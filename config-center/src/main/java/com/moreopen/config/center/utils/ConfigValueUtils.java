package com.moreopen.config.center.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public class ConfigValueUtils {
	
	private static final char HUANHANG = 10;
	
	private static final char HUICHE = 13;
	
	public static String replaceChar1013(String source) {
		if (StringUtils.isBlank(source)) {
			return source;
		}
		String str = source;
		int index = -1;
		while ((index = str.indexOf(HUANHANG)) != -1) {
			str = index < str.length() -1
					? str.substring(0, index) + "\\n" + str.substring(index + 1) 
						: str.substring(0, index) + "\\n";
		}
		while ((index = str.indexOf(HUICHE)) != -1) {
			str = index < str.length() -1
					? str.substring(0, index) + "\\r" + str.substring(index + 1)
						: str.substring(0, index) + "\\r";
		}
		return str;
	}
	
	public static void main(String[] args) {
		String ss = "aaa" + HUANHANG + "BBB" + HUICHE + "cccc" + HUANHANG + "dddd" + HUICHE + "ggggg" + HUANHANG;
		ss = replaceChar1013(ss);
		Assert.isTrue(ss.indexOf(HUANHANG) == -1);
		Assert.isTrue(ss.indexOf(HUICHE) == -1);
		System.out.println(ss);
	}

}
