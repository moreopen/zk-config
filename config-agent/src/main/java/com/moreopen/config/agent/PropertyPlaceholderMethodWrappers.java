package com.moreopen.config.agent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ReflectionUtils;

/**
 * store setters (WritedMethod) for placeholder properties
 */
@SuppressWarnings("all")
public class PropertyPlaceholderMethodWrappers {
	
	private static Logger logger = LoggerFactory.getLogger(PropertyPlaceholderMethodWrappers.class);
	
	private static Map<String, Set<MethodWrapper>> bean2methods = new HashMap<String, Set<MethodWrapper>>();
	
	public static void add(String beanName, BeanDefinition beanDefinition, String property, String propertyKey) {
		Set<MethodWrapper> methods = bean2methods.get(beanName);
		if (methods == null) {
			synchronized (beanName.intern()) {
				if ((methods = bean2methods.get(beanName)) == null) {
					methods = new HashSet<MethodWrapper>();
					bean2methods.put(beanName, methods);
				}
			}
		}
		
		try {
			Method method = ReflectionUtils.findMethod(
					Class.forName(beanDefinition.getBeanClassName()), 
					getSetterName(property), 
					null
				);
			if (method == null) {
				logger.error(String.format("can't find setter for property [%s]", property));
				return;
			} 
			methods.add(new MethodWrapper(method, propertyKey));
			if (logger.isInfoEnabled()) {
				logger.info(String.format("add method [%s], key [%s]", method, propertyKey));
			}
		} catch (ClassNotFoundException e) {
			logger.error("error", e);
		}
	}

	private static String getSetterName(String property) {
		return "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
	}

	public static boolean contains(String beanName) {
		return bean2methods.containsKey(beanName);
	}

	public static MethodWrapper[] get(String beanName) {
		return bean2methods.get(beanName).toArray(new MethodWrapper[0]);
	}
	
	public static class MethodWrapper {
		
		private Method method;
		
		private String propertyKey;

		public MethodWrapper(Method method, String propertyKey) {
			this.method = method;
			this.propertyKey = propertyKey;
		}

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public String getPropertyKey() {
			return propertyKey;
		}

		public void setPropertyKey(String propertyKey) {
			this.propertyKey = propertyKey;
		}
		
	}
}
