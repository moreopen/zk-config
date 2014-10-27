package com.moreopen.config.center.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.moreopen.config.center.domain.User;
import com.moreopen.config.center.service.UserServiceImpl;
import com.moreopen.config.center.utils.SessionContextUtils;

/**
 * 登录控制器
 * 
 */
@Controller
public class LoginController extends BaseController {

	@Resource
	private UserServiceImpl userServiceImpl;

	/**
	 * 校验用户密码。并进行登录
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/login", method = POST)
	public void login(HttpServletRequest request, HttpServletResponse response) {
		
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		try {
			User user = userServiceImpl.queryUserByUserNameAndPW(userName, password);
			if (user == null) {
				response.sendRedirect(request.getContextPath());
			} else {
				// @XXX TODO 暂时先这样，以后多机器的时候要考虑分布式session的管理机制
				HttpSession session = request.getSession();
				// put all user related property to session
				SessionContextUtils.setUser(session, user);
				response.sendRedirect("main.htm");
			}
		} catch (Exception e) {
			logger.error("login failed", e);
		}
	}
	
	@RequestMapping(value="/main")
	public ModelAndView main(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("/jsp/main");
	}

	/**
	 * 退出登录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout", method = GET)
	public void exit(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HttpSession session = request.getSession();
		session.invalidate();
		response.sendRedirect("loginPage.htm");
	}
	
	/**
	 * 跳到登陆界面
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/loginPage",method = GET)
	public ModelAndView toLoginPage(){
		ModelAndView mv=new ModelAndView("../login");
		return mv;
	}

}
