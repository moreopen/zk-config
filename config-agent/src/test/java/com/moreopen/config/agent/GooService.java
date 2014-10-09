package com.moreopen.config.agent;

import com.moreopen.config.agent.annotation.ZkConfig;

public class GooService extends FooService {
	
	private String msg;
	
	@ZkConfig(key="goo.msg")
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

}
