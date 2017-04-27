package com.moreopen.config.agent;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * util class for Placeholder (not support nested Placeholder)
 */
public class PlaceholderUtils {

	public static boolean isPlaceholderProperty(String property) {
		return property != null 
				&& property.startsWith(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX)
					&& property.endsWith(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX);
	}
	
	//trim placeholder prefix and suffix
	public static String trimPlaceholder(String property) {
		int index = property.indexOf(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX);
		property = property.substring(index + PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX.length());
		index = property.indexOf(PropertyPlaceholderConfigurer.DEFAULT_VALUE_SEPARATOR);
		if (index == -1) { 
			index = property.lastIndexOf(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX);
		}
		return property.substring(0, index);
	}
	
	public static Set<String> resolvePlaceholderProperties(String strVal) {

		Set<String> set = new HashSet<String>();
		int startIndex = strVal.indexOf(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX);
		while (startIndex != -1) {
			int endIndex = strVal.indexOf(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX);
			if (endIndex != -1) {
				String placeholder = strVal.substring(startIndex + PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX.length(), endIndex);
				int separatorIndex = placeholder.indexOf(PropertyPlaceholderConfigurer.DEFAULT_VALUE_SEPARATOR);
				if (separatorIndex != -1) {
					placeholder = placeholder.substring(0, separatorIndex);
				}
				if (org.apache.commons.lang.StringUtils.isNotBlank(placeholder)) {
					set.add(placeholder);
				}
				try {
					strVal = strVal.substring(endIndex + PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX.length() + 1);
					startIndex = strVal.indexOf(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX);
				} catch (Exception e) {
					startIndex = -1;
				}
			} else {
				startIndex = -1;
			}
		}
		return set;
	}
	
	public static void main(String[] args) {
		Set<String> set = resolvePlaceholderProperties("gggg${abb}bbb${bbb.c}dddd${ng}dd");
		System.out.println(set);
	}

}
