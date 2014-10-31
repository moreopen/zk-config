package com.moreopen.config.center.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.moreopen.config.center.spi.ConfigItem;
import com.moreopen.config.center.spi.VirtualPathHolder;
import com.moreopen.config.center.spi.ZKDelegate;
import com.moreopen.config.center.utils.ConfigValueUtils;
import com.moreopen.config.center.utils.Constants;

@Component
public class ConfigServiceImpl {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private ZKDelegate zkDelegate;
	
	@Resource
	private VirtualPathHolder virtualPathHolder;

	@SuppressWarnings("unchecked")
	public List<ConfigItem> getConfigItems(String pNodeVirtualPath) {
		if (StringUtils.isBlank(pNodeVirtualPath)) {
			List<ConfigItem> configItems = new ArrayList<ConfigItem>(1);
			configItems.add(createTopConfigItem());
			return configItems;
		}
		String pNodePath = virtualPathHolder.getPath(pNodeVirtualPath);
		List<String> subNodes = zkDelegate.getSubNodes(pNodePath);
		if (CollectionUtils.isEmpty(subNodes)) {
			return Collections.EMPTY_LIST;
		}
		List<ConfigItem> configItems = new ArrayList<ConfigItem>();
		for (String node : subNodes) {
			ConfigItem item = new ConfigItem();
			item.setPath(pNodePath + Constants.SLASH + node);
			item.setKey(node);
			item.setValue(zkDelegate.getValue(item.getPath()));
			item.setParent(zkDelegate.isParent(item.getPath()));
			item.setVirtualPath(virtualPathHolder.buildVirtualPath(item.getPath()));
			configItems.add(item);
		}
		return configItems;
	}

	private ConfigItem createTopConfigItem() {
		ConfigItem topItem = new ConfigItem();
		topItem.setPath(Constants.CONFIG_TOP_NODE);
		topItem.setKey(Constants.CONFIG_TOP_NODE);
		topItem.setValue(zkDelegate.getValue(topItem.getPath()));
		topItem.setParent(zkDelegate.isParent(topItem.getPath()));
		topItem.setVirtualPath(virtualPathHolder.buildVirtualPath(topItem.getPath()));
		return topItem;
	}

	public boolean update(String virtualPath, String value) {
		String nodePath = virtualPathHolder.getPath(virtualPath);
		if (StringUtils.isNotBlank(value)) {
			value = ConfigValueUtils.replaceChar1013(value);
		}
		try {
			zkDelegate.update(nodePath, value);
			return true;
		} catch (Exception e) {
			logger.error(String.format("update failed, node [%s], value [%s]", nodePath, value), e);
			return false;
		}
	}

	public boolean add(String pNodeVirtualPath, String key, String value) {
		String pNodePath = virtualPathHolder.getPath(pNodeVirtualPath);
		if (StringUtils.isNotBlank(value)) {
			value = ConfigValueUtils.replaceChar1013(value);
		}
		try {
			zkDelegate.add(pNodePath, key, value);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("added node: pNode [%s], node [%s], value [%s]", pNodePath, key, value));
			}
			return true;
		} catch (Exception e) {
			logger.error(String.format("add failed, pNode [%s], node [%s], value [%s]", pNodePath, key, value), e);
			return false;
		}
	}

	public boolean remove(String nodeVirtualPath) {
		String nodePath = virtualPathHolder.getPath(nodeVirtualPath);
		try {
			zkDelegate.remove(nodePath);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("removed node [%s]", nodePath));
			}
			return true;
		} catch (Exception e) {
			logger.error(String.format("remove node [%s] failed", nodePath), e);
			return false;
		}
	}

}
