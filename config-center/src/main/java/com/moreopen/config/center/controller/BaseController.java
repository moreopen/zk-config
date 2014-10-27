package com.moreopen.config.center.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 将结果输出到客户端
	 * @param response
	 * @param result
	 * @throws IOException
	 */
	protected void outputResult2Client(HttpServletResponse response, String result) throws IOException {
		response.setContentType("text/html;charset=UTF-8"); 
		response.getWriter().print(result);
	}
	
	/**
	 * 将结果输出到客户端
	 * @param response
	 * @param result
	 * @throws IOException
	 */
	protected void outputResult2Client(HttpServletResponse response, byte[] bytes) throws IOException {
		outputResult2Client(response, new String(bytes, "UTF-8"));
	}
	
}
