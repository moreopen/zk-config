package com.moreopen.config.agent.zk;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * processor for zk config updated
 */
public class ZkConfigAwaredMethodProcessor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Set<KeyValue>> holder = new HashMap<String, Set<KeyValue>>();

	public void put(String key, Method method, Object bean) {
		Set<KeyValue> keyValues = holder.get(key);
		if (keyValues == null) {
			synchronized (key.intern()) {
				if ((keyValues = holder.get(key)) == null) {
					keyValues = new HashSet<KeyValue>();
					holder.put(key, keyValues);
				}
			}
		}
		synchronized (method) {
			for (KeyValue keyValue : keyValues) {
				if (keyValue.getKey() == method) {
					return;
				}
			}
			keyValues.add(new DefaultKeyValue(method, bean));			
		}
	}

	public boolean process(String key, String value) {
		if (value == null) {
			return false;
		}
		Set<KeyValue> keyValues = holder.get(key);
		if (CollectionUtils.isEmpty(keyValues)) {
			logger.warn(String.format("can't find annotationed method&bean by ZkConfig, key [%s]", key));
			return false;
		}
		for (KeyValue keyValue : keyValues) {
			Method method = (Method) keyValue.getKey();
			Object bean = keyValue.getValue();
			Class<?>[] parameterTypes = method.getParameterTypes();
			Assert.isTrue(parameterTypes.length == 1);
			Class<?> parameterType = parameterTypes[0];
			try {
				if (parameterType == int.class || parameterType == Integer.class) {
					method.invoke(bean, Integer.parseInt(value));
				} else if(parameterType == double.class || parameterType == Double.class) {
					method.invoke(bean, Double.parseDouble(value));
				} else if (parameterType == long.class || parameterType == Long.class) {
					method.invoke(bean, Long.parseLong(value));
				} else if (parameterType == boolean.class || parameterType == Boolean.class) {
					method.invoke(bean, Boolean.parseBoolean(value));
				} else {
					method.invoke(bean, value);
				}
			} catch (Exception e) {
				logger.error(String.format("[%s] invoked method [%s] failed, arg [%s]", bean, method, value), e);
				return false;
			}
			if (logger.isInfoEnabled()) {
				logger.info(String.format("[%s] invoked method [%s] succeed, arg [%s]", bean, method, value));
			}
		}
		return true;
	}

	public Set<String> keys() {
		return holder.keySet();
	} 

}
