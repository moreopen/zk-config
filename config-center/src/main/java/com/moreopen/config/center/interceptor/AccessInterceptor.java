package com.moreopen.config.center.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.moreopen.config.center.domain.User;
import com.moreopen.config.center.utils.SessionContextUtils;

public class AccessInterceptor implements HandlerInterceptor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("================== preHandler : " + request.getRequestURL().toString());
		}
		HttpSession session = request.getSession();
		User user = SessionContextUtils.getUser(session);
		if (user == null) {
			response.sendRedirect("loginPage.htm");
			return false;
		}
		return true;

	}
}
