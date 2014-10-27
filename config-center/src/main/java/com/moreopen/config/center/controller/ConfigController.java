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
import com.moreopen.config.center.utils.Constants;

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
		
		String pNode = request.getParameter("id");
		if (pNode == null) {
			pNode = Constants.CONFIG_TOP_NODE;
		}
		List<ConfigItem> subNodes = configService.getConfigItems(pNode);
		String result = com.moreopen.config.center.utils.Tree.toJson(subNodes);
		
		outputResult2Client(response, result);
	}
	
	/** 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/saveConfig", method = POST)
	public void saveConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String nodeId = request.getParameter("nodeId");
		String value = request.getParameter("value");
		if (StringUtils.isBlank(nodeId)) {
			logger.warn("node id is required");
			return;
		}
		boolean result = configService.update(nodeId, value);
		outputResult2Client(response, result + "");
	}

}
