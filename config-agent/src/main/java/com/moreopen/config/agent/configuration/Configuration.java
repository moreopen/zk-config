package com.moreopen.config.agent.configuration;

import java.util.Properties;

/**
 * can implements this interface to read/write configuration
 */
public interface Configuration {

	void set(String key, String value);

	String get(String key);
	
	void init(Properties properties) throws Exception;
	
	Properties loadAll();

}