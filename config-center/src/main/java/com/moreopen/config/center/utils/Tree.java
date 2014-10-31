package com.moreopen.config.center.utils;

import java.util.List;

import com.moreopen.config.center.spi.ConfigItem;

public class Tree {

	/**
	 * XXX 生成节点时，以 virtualPath 作为 id，避免操作 tree 时 jquery 因为 "/" 字符报错
	 */
	public static String toJson(List<ConfigItem> items) {
		StringBuffer strB=new StringBuffer();
		strB.append("[");
		for (ConfigItem item : items) {
			strB.append("{\"id\":\"").append(item.getVirtualPath()).append("\",");
			strB.append("\"text\":\"").append(item.getKey()).append("\",");
			strB.append("\"parent\":\"").append(item.isParent()).append("\",");
			strB.append("\"value\":\"").append(item.getValue()).append("\"");
			//strB.append("\"attributes\":{\"value\":\"").append(item.getValue()).append("\"}");
			if (item.isParent()) {//不是叶子菜单就保持关闭状态
				strB.append(",\"state\":\"closed\"");//定义状态
			}
			strB.append("},");
		}
		strB.append("]");
		return strB.toString().replaceAll(",]", "]");
	}

}
