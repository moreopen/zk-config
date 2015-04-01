package com.moreopen.config.agent.configuration;

import static com.moreopen.config.agent.Constants.TOP_NODE;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.moreopen.config.agent.Constants;
import com.moreopen.config.agent.PlaceholderUtils;
import com.moreopen.config.agent.zk.ZkConfigAwaredMethodProcessor;
import com.moreopen.config.agent.zk.ZkValueChangedWatcher;

public class ZkBasedConfiguration implements Configuration {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String zkServerPort;
	
	private String app;
	
	private ZkClient zkClient;
	
	private ZkConfigAwaredMethodProcessor methodProcessor;
	
	private String appNode;
	
	private ZkValueChangedWatcher valueChangedWatcher;
	
	private Configuration localConfiguration;
	
	private boolean inited;
	
	@Override
	public void init(Properties properties) throws Exception {
		synchronized (this) {
			if (inited) {
				logger.warn("ZkBasedConfiguration has been inited, just return");
				return;
			}
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
			
			zkClient = new ZkClient();			
			zkClient.connect();
			valueChangedWatcher = new ZkValueChangedWatcher(zkClient, methodProcessor, localConfiguration);
			
			inited = true;
		}
	}
	
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
			value = new String(zkClient.getZk().getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("set valueChangedWatcher to key [%s]", key));
			}
		} catch (KeeperException ke) { 
			logger.warn(String.format("get data failed, node [%s], error [%s], fetch value from local configurator", keyNode, ke.getMessage()));
			//fetch local value
			value = localConfiguration.get(key);
		} catch (Exception e) {
			logger.error(String.format("get data failed, node [%s], fetch value from local configurator", keyNode), e);
			//fetch local value
			value = localConfiguration.get(key);
		}
		if (logger.isInfoEnabled()) {
			logger.info(String.format("get data result, key [%s] -- value [%s]", key, value));
		}
		return value;
	}
	
	//XXX merge zk config with local config
	@Override
	public Properties loadAll() {
		Properties properties = localConfiguration.loadAll();
		List<String> children = null;
		try {
			children = zkClient.getZk().getChildren(appNode, false);
		} catch (KeeperException ke) {
			logger.error(String.format("get children failed, appNode [%s], error [%s]", appNode, ke.getMessage()));
		} catch (Exception e) {
			logger.error(String.format("get children failed, appNode [%s]", appNode, e));
		}
		
		if (CollectionUtils.isNotEmpty(children)) {
			for (String child : children) {
				properties.put(child, get(child));
			}
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

	public class ZkClient implements Watcher {
		
		private int sessionTimeout = 3000;
		
		private int maxCheckTimes = 5;
		
		private boolean connected = false;
		
		private ZooKeeper zk;
		
		public ZkClient() throws Exception {
		}
				
		public void setConnected(boolean flag) {
			synchronized (this) {
				connected = flag;
			}
		}
		
		public void connect() throws IOException, InterruptedException, KeeperException {
			synchronized (this) {
				if (connected) {
					logger.warn("zkClient has connected, just return ...");
					return;
				}
				//async connect
				zk = new ZooKeeper(zkServerPort, sessionTimeout, this);
				int checkTimes = 0;
				while(!zk.getState().isConnected() && checkTimes < maxCheckTimes) {
					checkTimes++;
					if (logger.isInfoEnabled()) {
						logger.info(String.format("waiting for async connecting to zk [%s]", zkServerPort));
					}
					Thread.sleep(1000);
				}
				if (!zk.getState().isConnected()) {
					logger.warn(String.format("can't connect to zk [%s]", zkServerPort));
				} else {
					//if connected, do check topNode and appNode exist or not 
					Stat stat = zk.exists(TOP_NODE, false);
					Assert.notNull(stat, String.format("top node [%s] does not exist, plz init in zk [%s]", TOP_NODE, zk));
					stat = zk.exists(appNode, false);
					Assert.notNull(stat, String.format("app node [%s] does not exist, plz init in zk [%s]", appNode, zk));
				}
			}
		}
		
		@Override
		public void process(WatchedEvent event) {
			
			if (event.getState() == KeeperState.SyncConnected) {
				if (logger.isInfoEnabled()) {
					logger.info("===============sync connected to zk server");
				}
				setConnected(true);
				if (inited) {
					//invoke method when reconnected to zk
					Set<String> keys = methodProcessor.keys();
					for (String key : keys) {
						String keyNode = appNode + Constants.SLASH + key;
						String value = null;
						try {
							//fetch from zk
							value = new String(zk.getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
							if (logger.isInfoEnabled()) {
								logger.info(String.format("set valueChangedWatcher to key [%s], current value [%s]", key, value));
							}
							if (methodProcessor.process(key, value)) {
								//reset local config
								set(key, value);
							}
							
						} catch (NoNodeException nne) { 
							logger.warn(String.format("node [%s] does not exist", keyNode));
							continue;
						} catch (Exception e) {
							logger.error(String.format("get data failed, node [%s], fetch value from local configurator", keyNode), e);
							continue;
						}
					}
				}
				
			} else if (event.getState() == KeeperState.Disconnected) {
				setConnected(false);
				if (logger.isInfoEnabled()) {
					logger.info("===============disconnected from zk server");
				}
			}
		}
		
		public ZooKeeper getZk() {
			return this.zk;
		}
		
	}
	

}
