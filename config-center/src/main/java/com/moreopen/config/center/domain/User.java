package com.moreopen.config.center.domain;


public class User extends BaseDomain {
	
	private long id;
	
	private String name;
	
	private String password;
	
	public long getId() {
		return id;
	}
	public void setId(long userId) {
		this.id = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String userName) {
		this.name = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
		
}
