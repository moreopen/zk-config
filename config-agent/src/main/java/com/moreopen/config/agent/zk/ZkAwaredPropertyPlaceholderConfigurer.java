package com.moreopen.config.agent.zk;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;

import com.moreopen.config.agent.PlaceholderUtils;
import com.moreopen.config.agent.PropertyPlaceholderMethodWrappers;
import com.moreopen.config.agent.configuration.ZkBasedConfiguration;

public class ZkAwaredPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private ZkBasedConfiguration zkBasedConfiguration;
	
	@Override
	protected Properties mergeProperties() throws IOException {
		//XXX merge properties from ZK
		Properties properties = super.mergeProperties();
		try {
			zkBasedConfiguration.init(properties);
		} catch (Exception e) {
			throw new RuntimeException("zkBasedConfiguration init failed", e);
		}
		Properties zkProperties = zkBasedConfiguration.loadAll();
		properties.putAll(zkProperties);
		return properties;
	}

	public void setZkBasedConfiguration(ZkBasedConfiguration zkConfiguration) {
		this.zkBasedConfiguration = zkConfiguration;
	}
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		String[] beanDefinitionNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String beanName : beanDefinitionNames) {
			BeanDefinition beanDefinition = beanFactoryToProcess.getBeanDefinition(beanName);
			List<PropertyValue> propertyValueList = beanDefinition.getPropertyValues().getPropertyValueList();
			for (PropertyValue propertyValue : propertyValueList) {
				if (TypedStringValue.class.isInstance(propertyValue.getValue())) {
					String value = ((TypedStringValue) propertyValue.getValue()).getValue();
					if (PlaceholderUtils.isPlaceholderProperty(value)) {
						PropertyPlaceholderMethodWrappers.add(beanName, beanDefinition, propertyValue.getName(), PlaceholderUtils.trimPlaceholder(value));
					}
				}
			}
			
			//unsupport indexed constructor-arguments
			List<ValueHolder> genericArgumentValues = beanDefinition.getConstructorArgumentValues().getGenericArgumentValues();
			for (ValueHolder valueHolder : genericArgumentValues) {
				if (TypedStringValue.class.isInstance(valueHolder.getValue())) {
					String value = ((TypedStringValue) valueHolder.getValue()).getValue();
					if (PlaceholderUtils.isPlaceholderProperty(value)) {
						PropertyPlaceholderMethodWrappers.add(beanName, beanDefinition, valueHolder.getName(), PlaceholderUtils.trimPlaceholder(value));
					}
				}
			}
			
		}
		super.processProperties(beanFactoryToProcess, props);
	}

	
}
