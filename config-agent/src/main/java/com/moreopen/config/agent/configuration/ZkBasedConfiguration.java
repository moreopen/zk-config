package com.moreopen.config.agent.configuration;

import static com.moreopen.config.agent.Constants.TOP_NODE;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
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
	
	private static final String CONFIG_SYSTEM_FILE = "/opt/libs/trace/env.ini"; 
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * comma separated host:port pairs, each corresponding to a zk
     *            server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002"
	 */
	private String zkServerPort;
	
	private String app;
	
	private ZkClient zkClient;
	
	private ZkConfigAwaredMethodProcessor methodProcessor;
	
	private String appNode;
	
	private ZkValueChangedWatcher valueChangedWatcher;
	
	private Configuration localConfiguration;
	
	private boolean inited;
	
	/**
	 * 存放全局变量的应用名
	 */
	private String globalApp;
	
	private String globalNode;
	
	@Override
	public void init(Properties properties) throws Exception {
		synchronized (this) {
			if (inited) {
				logger.warn("ZkBasedConfiguration has been inited, just return");
				return;
			}
			
			if (PlaceholderUtils.isPlaceholderProperty(zkServerPort)) {
				zkServerPort = properties.getProperty(PlaceholderUtils.trimPlaceholder(zkServerPort));
			}
			if (StringUtils.isBlank(zkServerPort)) {
				//从指定系统文件中获取,推荐此方式
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(CONFIG_SYSTEM_FILE);
					Properties props = new Properties();
					props.load(fis);
					zkServerPort = props.getProperty("zkServerPort");
					logger.info("====================== load zkServerPort from file : " + CONFIG_SYSTEM_FILE + ", value : " + zkServerPort);
				} catch (Exception e) {
					logger.warn("get zkServerPort from config system file failed", e);
				} finally {
					IOUtils.closeQuietly(fis);
				}
			}
			
			if (PlaceholderUtils.isPlaceholderProperty(app)) {
				app = properties.getProperty(PlaceholderUtils.trimPlaceholder(app));
			}
			
			if (PlaceholderUtils.isPlaceholderProperty(globalApp)) {
				globalApp = properties.getProperty(PlaceholderUtils.trimPlaceholder(globalApp));
			}
			
			Assert.notNull(app);
			Assert.notNull(zkServerPort);
			Assert.notNull(localConfiguration);
			localConfiguration.init(properties);
			
			appNode = app.startsWith(Constants.SLASH) ? (TOP_NODE + app) : TOP_NODE + Constants.SLASH + app;
			if (StringUtils.isNotBlank(globalApp)) {
				globalNode = globalApp.startsWith(Constants.SLASH) ? (TOP_NODE + globalApp) : TOP_NODE + Constants.SLASH + globalApp;
			}
			methodProcessor = new ZkConfigAwaredMethodProcessor();
			
			zkClient = new ZkClient();
			zkClient.connect();
			valueChangedWatcher = new ZkValueChangedWatcher(zkClient, methodProcessor, localConfiguration);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("ZkBasedConfiguration inited, appNode : [%s], globalNode : [%s]", appNode, globalNode));
			}
			inited = true;
		}
	}
	
	@Override
	public void set(String key, String value) {
		localConfiguration.set(key, value);
	}

	/**
	 * XXX 逻辑特殊，请谨慎修改！！！
	 * 1.先从 zk app node 下获取
	 * 2.若 1 没获取到值，则从 zk global node 下获取
	 * 3.若 1、2 没获取到值则从本地获取并设置值到 zk app node
	 * 4.若有访问异常则直接从本地获取
	*/
	@Override
	public String get(String key) {
		try {
			String value = simpleGetFromNode(key, appNode);
			if (value == null && isGlobalNodeAwared()) {
				value = simpleGetFromNode(key, globalNode);
			}
			if (value == null) {
				//get from local and reset to appNode
				value = localConfiguration.get(key);
				if (value != null) {
					createZKNode(buildKeyNode(appNode, key), value);
				}
			}
			return value;
		} catch (Exception e) {
			//just get from local but not reset to zk
			return localConfiguration.get(key);
		}
	}
	
	private boolean isGlobalNodeAwared() {
		return StringUtils.isNotBlank(globalNode);
	}
	
	
	private String simpleGetFromNode(String key, String node) throws Exception{
		String keyNode = buildKeyNode(node, key);
		String value = null;
		try {
			//fetch from zk
			Stat stat = zkClient.getZk().exists(keyNode, null);
			if (stat == null) {
				logger.warn(String.format("simpleGetFromNode failed, node [%s] not exists", keyNode));
				return null;
			}
			value = new String(zkClient.getZk().getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("simpleGetFromNode result and set valueChangedWatcher key [%s] -- value [%s]", keyNode, value));
			}
			return value;
		} catch (Exception ke) {
			logger.warn(String.format("simpleGetFromNode failed, node [%s], error [%s]", keyNode, ke.getMessage())); 
			throw ke;
		}
	}

	private String getFromNode(String key, String node) {
		String keyNode = buildKeyNode(node, key);
		String value = null;
		try {
			//fetch from zk
			value = new String(zkClient.getZk().getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("set valueChangedWatcher to key [%s]", keyNode));
			}
		} catch (KeeperException ke) {
			logger.warn(String.format("get data failed, node [%s], error [%s], fetch value from local configurator", keyNode, ke.getMessage()));
			//fetch local value
			value = localConfiguration.get(key);
			//XXX set value to zk, add watcher to the node
			if (value != null) {
				createZKNode(keyNode, value);
			} 
		} catch (Exception e) {
			logger.error(String.format("get data failed, node [%s], fetch value from local configurator", keyNode), e);
			//fetch local value
			value = localConfiguration.get(key);
		}
		if (logger.isInfoEnabled()) {
			logger.info(String.format("get data result, key [%s] -- value [%s]", keyNode, value));
		}
		return value;
	}

	private void createZKNode(String keyNode, String value) {
		try {
			zkClient.getZk().create(keyNode, value.getBytes(Constants.UTF8), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
			//set value changed watcher on node
			zkClient.getZk().getData(keyNode, valueChangedWatcher, null);
			logger.warn(String.format("set zk node [%s] with local config value [%s] succeed", keyNode, value));
		} catch (Exception e) {
			logger.error(String.format("set zk node [%s] with local config value [%s] error", keyNode, value), e);
		}
	}
	
	//XXX merge zk config with local config
	@Override
	public Properties loadAll() {
		//1. load from local
		Properties properties = new Properties();
		Properties localProperties = localConfiguration.loadAll();
		for (Object key : localProperties.keySet()) {
			properties.put(key, localProperties.get(key));
		}
		//2. load from app node
		loadFromZK(properties, appNode, null);

		//3. load from global node not exist in above
		if (isGlobalNodeAwared()) {
			try {
				loadFromZK(properties, globalNode, zkClient.getZk().getChildren(appNode, false));
			} catch (Exception e) {
				logger.error("load exception", e);
			}
		}
		//4. reset local
		localConfiguration.reset(properties);
		return properties;
	}

	private void loadFromZK(Properties properties, String node, List<String> ignoredKeys) {
		List<String> children = null;
		try {
			children = zkClient.getZk().getChildren(node, false);
		} catch (KeeperException ke) {
			logger.error(String.format("get children failed, node [%s], error [%s]", node, ke.getMessage()));
		} catch (Exception e) {
			logger.error(String.format("get children failed, node [%s]", node, e));
		}
		
		if (CollectionUtils.isNotEmpty(children)) {
			for (String child : children) {
				if (ignoredKeys != null && ignoredKeys.contains(child)) {
					continue;
				}
				properties.put(child, getFromNode(child, node));
			}
		}
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
	
	public void setGlobalApp(String globalApp) {
		this.globalApp = globalApp;
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
//					Assert.notNull(stat, String.format("top node [%s] does not exist, plz init in zk [%s]", TOP_NODE, zk));
					if (stat == null) {
						//create if top node is null
						zk.create(TOP_NODE, null, Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
						logger.warn(String.format("top node [%s] does not exist, created", TOP_NODE));
					}
					stat = zk.exists(appNode, false);
//					Assert.notNull(stat, String.format("app node [%s] does not exist, plz init in zk [%s]", appNode, zk));
					if (stat == null) {
						zk.create(appNode, null, Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
						logger.warn(String.format("app node [%s] does not exist, created", appNode));
					}
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
						String keyNode = buildKeyNode(appNode, key);
						String value = null;
						try {
							Stat stat = zk.exists(keyNode, null);
							if (stat == null && isGlobalNodeAwared()) {
								keyNode = buildKeyNode(globalNode, key);
							} 
							//fetch from zk
							value = new String(zk.getData(keyNode, valueChangedWatcher, null), Constants.UTF8);
							if (logger.isInfoEnabled()) {
								logger.info(String.format("set valueChangedWatcher to key [%s], current value [%s]", keyNode, value));
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
					logger.info("===============disconnected from zk server and  reset zk client");
				}
				//XXX zk reset, force to reconnect when zk disconnected
				try {
					zk.close();
					zk = null;
					zkClient = new ZkClient();
					zkClient.connect();
				} catch (Exception e) {
					logger.error("zk reset and re-connect failed", e);
				}
			}
		}
		
		public ZooKeeper getZk() {
			return this.zk;
		}
		
	}
	
	private String buildKeyNode(String node, String key) {
		return node + Constants.SLASH + key;
	}

	@Override
	public void reset(Properties properties) {
		localConfiguration.reset(properties);
	}

}
