package com.moreopen.config.agent.configuration;

import static com.moreopen.config.agent.Constants.TOP_NODE;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.moreopen.config.agent.Constants;
import com.moreopen.config.agent.PlaceholderUtils;
import com.moreopen.config.agent.zk.DefaultWatcher;
import com.moreopen.config.agent.zk.ZkConfigAwaredMethodProcessor;
import com.moreopen.config.agent.zk.ZkValueChangedWatcher;

public class ZkBasedConfiguration implements Configuration {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String zkServerPort;
	
	private String app;
	
	private ZooKeeper zk;
	
	private ZkConfigAwaredMethodProcessor methodProcessor;
	
	private String appNode;
	
	private ZkValueChangedWatcher valueChangedWatcher;
	
	private int sessionTimeout = 3000;
	
	private int maxCheckTimes = 5;
	
	private Configuration localConfiguration;
	
	@Override
	public void set(String key, String value) {
		localConfiguration.set(key, value);
	}

	@Override
	public String get(String key) {
		String keyNode = appNode + Constants.SLASH + key;
		String value = null;
		try {
			//fetch from zk
			value = new String(zk.getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
		} catch (KeeperException ke) { 
			logger.warn(String.format("get data failed, node [%s], error [%s], fetch value from local configurator", keyNode, ke.getMessage()));
			//fetch local value
			value = localConfiguration.get(key);
		} catch (Exception e) {
			logger.error(String.format("get data failed, node [%s], fetch value from local configurator", keyNode), e);
			//fetch local value
			value = localConfiguration.get(key);
		}
		
		return value;
	}
	
	@Override
	public Properties loadAll() {
		Properties properties = new Properties();
		List<String> children = null;
		try {
			children = zk.getChildren(appNode, false);
		} catch (KeeperException ke) {
			logger.error(String.format("get children failed, appNode [%s], error [%s]", appNode, ke.getMessage()));
		} catch (Exception e) {
			logger.error(String.format("get children failed, appNode [%s]", appNode, e));
		}
		if (CollectionUtils.isNotEmpty(children)) {
			for (String child : children) {
				properties.put(child, get(child));
			}
		} else {
			properties = localConfiguration.loadAll();
		}
		return properties;
	}
	
	public ZkConfigAwaredMethodProcessor getMethodProcessor() {
		return this.methodProcessor;
	}
	
	public void setZkServerPort(String zkServerPort) {
		this.zkServerPort = zkServerPort;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void setLocalConfiguration(Configuration localConfiguration) {
		this.localConfiguration = localConfiguration;
	}

	@Override
	public void init(Properties properties) throws Exception {
		if (PlaceholderUtils.isPlaceholderProperty(app)) {
			app = properties.getProperty(PlaceholderUtils.trimPlaceholder(app));
		}
		if (PlaceholderUtils.isPlaceholderProperty(zkServerPort)) {
			zkServerPort = properties.getProperty(PlaceholderUtils.trimPlaceholder(zkServerPort));
		}
		
		Assert.notNull(app);
		Assert.notNull(zkServerPort);
		Assert.notNull(localConfiguration);
		localConfiguration.init(properties);
		
		appNode = app.startsWith(Constants.SLASH) ? (TOP_NODE + app) : TOP_NODE + Constants.SLASH + app;
		methodProcessor = new ZkConfigAwaredMethodProcessor();
		
		initZk();
	}

	private void initZk() throws IOException, InterruptedException, KeeperException {
		DefaultWatcher defaultWatcher = new DefaultWatcher();
		//async connect
		zk = new ZooKeeper(zkServerPort, sessionTimeout, defaultWatcher);
		int checkTimes = 0;
		while(!zk.getState().isConnected() && checkTimes < maxCheckTimes) {
			checkTimes++;
			if (logger.isInfoEnabled()) {
				logger.info(String.format("waiting for async connecting to zk [%s]", zkServerPort));
			}
			Thread.sleep(1000);
		}
		if (zk.getState().isConnected()) {
			//if connected, do check topNode and appNode exist or not 
			Stat stat = zk.exists(TOP_NODE, false);
			if (stat == null) {
				throw new RuntimeException(String.format("top node [%s] does not exist, plz init in zk [%s]", TOP_NODE, zkServerPort));
			}
			stat = zk.exists(appNode, false);
			if (stat == null) {
				throw new RuntimeException(String.format("app node [%s] does not exist, plz init in zk [%s]", appNode, zkServerPort));
			}
		} else {
			logger.warn(String.format("can't connect to zk [%s]", zkServerPort));
		}
		valueChangedWatcher = new ZkValueChangedWatcher(zk, methodProcessor, localConfiguration);
		defaultWatcher.init(methodProcessor, appNode, zk, valueChangedWatcher, localConfiguration);
	}

}
