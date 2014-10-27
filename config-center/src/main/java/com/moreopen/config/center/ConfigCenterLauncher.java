/*
 * Copyright 2011 y.sdo.com, Inc. All rights reserved.
 * y.sdo.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.moreopen.config.center;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Web Server Launcher
 */
public class ConfigCenterLauncher {

	private static  Logger logger = Logger.getLogger(ConfigCenterLauncher.class);

	private static Server server = null;
	private int port;

	public ConfigCenterLauncher(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		if (server != null) {
			return;
		}
		server = new Server(this.port);
		server.setThreadPool(getThreadPool());
		server.setStopAtShutdown(true);
		server.setSendServerVersion(true);
		server.setHandler(getWebAppContext());
		server.start();
		logger.info("Start icollector server, done.port: "+ this.port);
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	public void setPort(int port) {
		this.port = port;
	}

	private ThreadPool getThreadPool() {
		return new ExecutorThreadPool();
	}

	private WebAppContext getWebAppContext() {

		String path = ConfigCenterLauncher.class.getResource("/").getFile().replaceAll("/target/(.*)", "")
				+ "/src/main/webapp";

		return new WebAppContext(path, "/config");
	}

	public static void main(String[] args) throws Exception {
		final String PROFILE_NAME = "/app.properties";
		Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(ConfigCenterLauncher.class.getResource(PROFILE_NAME)));
		int port = Integer.parseInt(properties.getProperty("webserver.port"));
		if (port == 0) {
			logger.error("property: config.webserver.port not found, please check " + PROFILE_NAME);
			return;
		}
		// launch the monitor console server
		new ConfigCenterLauncher(port).run();
		server.join();
	}
}
