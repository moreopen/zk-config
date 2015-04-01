package com.moreopen.config.agent;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

@ContextConfiguration(locations="classpath:applicationContext.xml")
public class ZkConfigAgentTest extends AbstractJUnit4SpringContextTests {
	
	@Resource
	private FooService fooService;
	
	@Resource
	private GooService gooService;
	
	@Resource
	private HooService hooService;
	
	@Resource
	private JooService jooService;
	
	@Before
	public void before() {
		Assert.notNull(fooService);
		Assert.notNull(gooService);
		Assert.notNull(hooService);
		Assert.notNull(jooService);
	}
	
	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			System.out.println("foo ***********************");
			System.out.println("name : " + fooService.getName());
			System.out.println("times : " + fooService.getTimes());
			System.out.println("timeInMs : " + fooService.getTimeInMs());
			System.out.println("averageLevel : " + fooService.getAverageLevel());
			
			System.out.println("goo ***********************");
			System.out.println("name : " + gooService.getName());
			System.out.println("times : " + gooService.getTimes());
			System.out.println("timeInMs : " + gooService.getTimeInMs());
			System.out.println("averageLevel : " + gooService.getAverageLevel());
			System.out.println("msg : " + gooService.getMsg());
			
			System.out.println("hoo ***********************");
			System.out.println("url : " + hooService.getUrl());
			System.out.println("num : " + hooService.getNum());
			System.out.println("size : " + hooService.getSize());
			System.out.println("hoo.enable : " + hooService.isEnabled());
			System.out.println("hoo.name : " + hooService.getName());
			System.out.println("=================================================");
			
			System.out.println("joo ***********************");
			System.out.println("joo.type : " + jooService.getType());
			
			Thread.sleep(3000);
		}
		while(true);
	}
}
