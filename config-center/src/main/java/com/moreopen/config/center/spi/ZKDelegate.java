package com.moreopen.config.center.spi;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.moreopen.config.center.utils.Constants;

public class ZKDelegate implements InitializingBean {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String zkServerPort;
	
	private ZooKeeper zk;
	
	private int sessionTimeout = 3000;
	
	private int maxCheckTimes = 5;

	@SuppressWarnings("unchecked")
	public List<String> getSubNodes(String pNode) {
		try {
			List<String> children = zk.getChildren(pNode, false);
			return children;
		} catch (SessionExpiredException see) {
			logger.error(String.format("get children failed, pNode [%s]", pNode), see);
			try {
				connectZk();
			} catch (Exception e) {
				logger.error("connect failed", e);
			}
			return Collections.EMPTY_LIST;
		}
		catch (Exception e) {
			logger.error(String.format("get children failed, pNode [%s]", pNode), e);
			return Collections.EMPTY_LIST;
		} 
	}
	
	public String getValue(String node) {
		try {
			return new String(zk.getData(node, false, null), Constants.UTF8);
		} catch (Exception e) {
			logger.error(String.format("get data failed, node [%s]", node), e);
			return StringUtils.EMPTY;
		}
	}

	public boolean isParent(String node) {
		return CollectionUtils.isNotEmpty(getSubNodes(node));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//async connect
		connectZk();
	}

	private void connectZk() throws IOException, InterruptedException, KeeperException {
		zk = new ZooKeeper(zkServerPort, sessionTimeout, new Watcher() {
			
			@Override
			public void process(WatchedEvent event) {
				if (logger.isInfoEnabled()) {
					logger.info("================ watched event :" + event);
				}
			}
		});
		
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
			Stat stat = zk.exists(Constants.CONFIG_TOP_NODE, false);
			if (stat == null) {
				throw new RuntimeException(String.format("top node [%s] does not exist, plz init in zk [%s]", Constants.CONFIG_TOP_NODE, zkServerPort));
			}
		} else {
			logger.warn(String.format("can't connect to zk [%s]", zkServerPort));
		}
	}
	
	public void setZkServerPort(String zkServerPort) {
		this.zkServerPort = zkServerPort;
	}

	public void update(String nodePath, String value) throws Exception {
		String existedValue = getValue(nodePath);
		if (StringUtils.equals(value, existedValue)) {
			logger.warn(String.format("node [%s]'s value is same as the para value [%s], not do save", nodePath, value));
			return;
		}
		zk.setData(nodePath, value == null ? null : value.getBytes(Constants.UTF8), -1);
		if (logger.isInfoEnabled()) {
			logger.info(String.format("updated node [%s], new value [%s], old value [%s]", nodePath, value, existedValue));
		}
	}

	public void add(String pNodePath, String key, String value) throws Exception {
		String path = pNodePath + Constants.SLASH + key;
		zk.create(path, value == null ? null : value.getBytes(Constants.UTF8), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
	}

	public void remove(String nodePath) throws Exception {
		zk.delete(nodePath, -1);
	}

}
