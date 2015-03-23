package com.moreopen.config.agent;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * util class for Placeholder (not support nested Placeholder)
 */
public class PlaceholderUtils {

	public static boolean isPlaceholderProperty(String property) {
		return property.startsWith(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX)
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

}
