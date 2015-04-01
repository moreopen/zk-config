package com.moreopen.config.agent;

import org.springframework.beans.factory.InitializingBean;

public class HooService implements InitializingBean {
	
	private String url;
	
	private int num;
	
	private int size;
	
	private boolean enabled;
	
	private String name;
	
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public HooService() {
		System.out.println("new instance");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("================= hoo url : " + url);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
