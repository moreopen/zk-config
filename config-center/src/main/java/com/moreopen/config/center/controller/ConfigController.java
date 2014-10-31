package com.moreopen.config.center.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.moreopen.config.center.service.ConfigServiceImpl;
import com.moreopen.config.center.spi.ConfigItem;

@Controller
public class ConfigController extends BaseController {
	
	@Resource
	private ConfigServiceImpl configService;
	
	/** 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/getConfigItems", method = POST)
	public void getConfigItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String virtualPath = request.getParameter("id");
		List<ConfigItem> subNodes = configService.getConfigItems(virtualPath);
		String result = com.moreopen.config.center.utils.Tree.toJson(subNodes);
		if (logger.isDebugEnabled()) {
			logger.debug("configs : " + result);
		}
		
		outputResult2Client(response, result);
	}
	
	/** 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/saveConfig", method = POST)
	public void saveConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String nodeVirtualPath = request.getParameter("nodeVirtualPath");
		if (StringUtils.isBlank(nodeVirtualPath)) {
			logger.warn("node id is required");
			return;
		}
		String value = request.getParameter("value");
		boolean result = configService.update(nodeVirtualPath, value);
		outputResult2Client(response, result + "");
	}
	
	/** 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/addConfig", method = POST)
	public void addConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String pNodeVirtualPath = request.getParameter("pNodeVirtualPath");
		String key = request.getParameter("key");
		if (StringUtils.isBlank(key)) {
			logger.warn("config key is required");
			return;
		}
		String value = request.getParameter("value");
		boolean result = configService.add(pNodeVirtualPath, key, value);
		outputResult2Client(response, result + "");
	}
	
	/** 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/removeConfig", method = POST)
	public void removeConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String nodeVirtualPath = request.getParameter("nodeVirtualPath");
		boolean result = configService.remove(nodeVirtualPath);
		outputResult2Client(response, result + "");
	}

}
