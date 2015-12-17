package com.moreopen.config.agent;

import javax.annotation.Resource;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations="classpath:applicationContext0.xml")
public class DepositeHelperTest extends AbstractJUnit4SpringContextTests {
	
	@Resource
	private DepositeHelper depositeHelper;
	
	@Test
	public void test() throws InterruptedException {
		
		for (int i = 0; i <100; i++) {
			depositeHelper.check(RandomStringUtils.randomAlphabetic(5), RandomUtils.nextInt(210000));
			Thread.sleep(2000);
		}
		
	}
	
	

}
