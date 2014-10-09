package com.moreopen.config.agent.zk;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.moreopen.config.agent.Constants;
import com.moreopen.config.agent.annotation.ZkConfig;
import com.moreopen.config.agent.configuration.Configuration;

/**
 * Watcher for configured node
 * if node value is changed, must invoke the {@link ZkConfig} ANNOTATIONED method and refresh the local configuration
 */
public class ZkValueChangedWatcher implements Watcher {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ZooKeeper zk;

	private ZkConfigAwaredMethodProcessor methodHolder;
	
	private Configuration localFileConfigurator;
	
	public ZkValueChangedWatcher(
			ZooKeeper zk, 
			ZkConfigAwaredMethodProcessor methodHolder, 
			Configuration localFileConfigurator) {
		this.zk = zk;
		this.methodHolder = methodHolder;
		this.localFileConfigurator = localFileConfigurator;
	}

	@Override
	public void process(WatchedEvent event) {
		if (logger.isInfoEnabled()) {
			logger.info(event.toString());
		}
		if (event.getType() != Event.EventType.NodeDataChanged) {
			return;
		} 
		
		String keyNode = event.getPath();
		String value = StringUtils.EMPTY;
		try {
			value = new String(zk.getData(keyNode, this, null), Constants.UTF8);
		} catch (KeeperException ke) {
			logger.error(String.format("get data failed, keyNode [%s], error [%s]", event.getPath()), ke.getMessage());
			return;
		} catch (Exception e) {
			logger.error(String.format("get data failed, keyNode [%s]", event.getPath()), e);
			return;
		}
		//invoke ZkConfigAnnotationed method
		int pos = keyNode.lastIndexOf(Constants.SLASH);
		String key = keyNode.substring(pos + 1);
		if (methodHolder.process(key, value)) {
			//reset local config
			localFileConfigurator.set(key, value);
		}
	}

}
