package com.moreopen.config.center.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.moreopen.config.center.spi.ConfigItem;
import com.moreopen.config.center.spi.ZKDelegate;
import com.moreopen.config.center.utils.Constants;

@Component
public class ConfigServiceImpl {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private ZKDelegate zkDelegate;

	@SuppressWarnings("unchecked")
	public List<ConfigItem> getConfigItems(String pNode) {
		List<String> subNodes = zkDelegate.getSubNodes(pNode);
		if (CollectionUtils.isEmpty(subNodes)) {
			return Collections.EMPTY_LIST;
		}
		List<ConfigItem> configItems = new ArrayList<ConfigItem>();
		for (String node : subNodes) {
			ConfigItem item = new ConfigItem();
			item.setId(pNode + Constants.SLASH + node);
			item.setText(node);
			item.setValue(zkDelegate.getValue(item.getId()));
			item.setParent(zkDelegate.isParent(item.getId()));
			configItems.add(item);
		}
		return configItems;
	}

	public boolean update(String nodeId, String value) {
		try {
			zkDelegate.update(nodeId, value);
			return true;
		} catch (Exception e) {
			logger.error(String.format("update failed, node [%s], value [%s]", nodeId, value), e);
			return false;
		}
	}

}
