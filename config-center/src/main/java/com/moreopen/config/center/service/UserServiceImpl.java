package com.moreopen.config.center.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.moreopen.config.center.dao.UserDAO;
import com.moreopen.config.center.domain.User;

@Component
public class UserServiceImpl {
	
	@Resource
	private UserDAO userDAO;

	public User queryUserByUserNameAndPW(String userName, String pwd) {
		return userDAO.findUser(userName, pwd);
	}

}
