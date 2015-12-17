package com.moreopen.config.agent;

import com.moreopen.config.agent.annotation.ZkConfig;


public class DepositeHelper {

	private int level = 100000;
	
	public void check(String name, int amout) {
		if (amout > level) {
			System.out.println(String.format("user [%s], saved [%s], is rich", name, amout));
		} else {
			System.out.println(String.format("user [%s], saved [%s], is common", name, amout));
		}
	}

	
	@ZkConfig(key="deposite.level")
	public void setLevel(int level) {
		this.level = level;
	}

}
