package com.moreopen.config.agent.zk;

import java.util.Set;

import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.moreopen.config.agent.Constants;
import com.moreopen.config.agent.configuration.Configuration;
import com.moreopen.config.agent.configuration.ZkBasedConfiguration.ZkClient;

/**
 * Default Watcher for ZooKeeper
 */
@Deprecated
public class DefaultWatcher implements Watcher {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ZkConfigAwaredMethodProcessor methodProcessor;
	
	private String appNode;
	
	private ZooKeeper zk;
	
	private ZkValueChangedWatcher valueChangedWatcher;
	
	private Configuration localConfigurator;
	
	private boolean inited;
	
	private ZkClient zkClient;

	public void init(
			ZkConfigAwaredMethodProcessor methodProcessor, 
			String appNode, 
			ZooKeeper zk, 
			ZkValueChangedWatcher valueChangedWatcher, 
			Configuration localConfigurator,
			ZkClient zkClient) {
		this.methodProcessor = methodProcessor;
		this.appNode = appNode;
		this.zk = zk;
		this.valueChangedWatcher = valueChangedWatcher;
		this.localConfigurator = localConfigurator;
		this.zkClient = zkClient;
		this.inited = true;
	}

	@Override
	public void process(WatchedEvent event) {
		if (logger.isInfoEnabled()) {
			logger.info("default watched event :" + event);
		}
		if (event.getState() == KeeperState.SyncConnected) {
			if (zkClient != null) {
				zkClient.setConnected(true);
			}
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
							localConfigurator.set(key, value);
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
			try {
				zkClient.setConnected(false);
				zkClient.connect();
			} catch (Exception e) {
				logger.error("connet to zk failed", e);
			}
		}
	}
}
