package com.moreopen.config.center.utils;

import javax.servlet.http.HttpSession;

import com.moreopen.config.center.domain.User;

public class SessionContextUtils {

	public static void setUser(HttpSession session, User user) {
		session.setAttribute("user", user);
	}

	public static User getUser(HttpSession session) {
		return (User) session.getAttribute("user");
	}

}
