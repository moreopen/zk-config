package com.moreopen.config.agent.zk;

import java.lang.reflect.Method;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;

import com.moreopen.config.agent.PropertyPlaceholderMethodWrappers;
import com.moreopen.config.agent.PropertyPlaceholderMethodWrappers.MethodWrapper;
import com.moreopen.config.agent.annotation.ZkConfig;
import com.moreopen.config.agent.configuration.ZkBasedConfiguration;

/**
 * customized BeanPostProcessor to init bean defined method with {@link ZkConfig} Annotation
 *  and init bean contained in PropertyPlaceholderMethodWrappers
 */
public class ZkAwaredBeanPostProcessor implements BeanPostProcessor, InitializingBean {
	
	private ZkBasedConfiguration zkBasedConfiguration;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		Assert.notNull(zkBasedConfiguration);
		zkBasedConfiguration.init(new Properties());
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Method[] declaredMethods = bean.getClass().getMethods();
		for (Method method : declaredMethods) {
			if (method.isAnnotationPresent(ZkConfig.class)) {
				ZkConfig annotation = method.getAnnotation(ZkConfig.class);
				processMethod(bean, method, annotation.key());
			}
		}
		if (PropertyPlaceholderMethodWrappers.contains(beanName)) {
			MethodWrapper[] methods = PropertyPlaceholderMethodWrappers.get(beanName);
			for (MethodWrapper wrapper : methods) {
				processMethod(bean, wrapper.getMethod(), wrapper.getPropertyKey());
			}
		}
		return bean;
	}

	private void processMethod(Object bean, Method method, String key) {
		zkBasedConfiguration.getMethodProcessor().put(key, method, bean);
		String value = zkBasedConfiguration.get(key);
		if (zkBasedConfiguration.getMethodProcessor().process(key, value)) {
			zkBasedConfiguration.set(key, value);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void setZkBasedConfiguration(ZkBasedConfiguration zkConfiguration) {
		this.zkBasedConfiguration = zkConfiguration;
	}
}
