package com.moreopen.config.center.spi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.moreopen.config.center.utils.Constants;

@Component
public class VirtualPathHolder {
	
	/**
	 * key : virtual path
	 * value : path
	 */
	private Map<String, String> virtualPath2Paths = new HashMap<String, String>();

	public String buildVirtualPath(String path) {
		try {
			String digested = DigestUtils.md5Hex(path.getBytes(Constants.UTF8));
			virtualPath2Paths.put(digested, path);
			return digested;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getPath(String virtualPath) {
		String path = virtualPath2Paths.get(virtualPath);
		Assert.notNull(path);
		return path;
	}

}
