package com.moreopen.config.agent;

import com.moreopen.config.agent.annotation.ZkConfig;

public class FooService {
	
	private String name;
	
	private int times;
	
	private long timeInMs;
	
	private double averageLevel;
	
	private int size;

	@ZkConfig(key="foo.name")
	public void setName(String name) {
		this.name = name;
	}

	@ZkConfig(key="foo.times")
	public void setTimes(int times) {
		this.times = times;
	}

	@ZkConfig(key="foo.timeInMs")
	public void setTimeInMs(long timeInMs) {
		this.timeInMs = timeInMs;
	}

	@ZkConfig(key="foo.averageLevel")
	public void setAverageLevel(double averageLevel) {
		this.averageLevel = averageLevel;
	}

	public String getName() {
		return name;
	}

	public int getTimes() {
		return times;
	}

	public long getTimeInMs() {
		return timeInMs;
	}

	public double getAverageLevel() {
		return averageLevel;
	}

	@ZkConfig(key = "hoo.size")
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return this.size;
	}

}
