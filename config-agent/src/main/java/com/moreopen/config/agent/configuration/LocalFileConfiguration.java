package com.moreopen.config.agent.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;

import com.moreopen.config.agent.PlaceholderUtils;


/**
 * configuration based on local file, to be used when zk is not work,
 * use map to cache all properties
 */
public class LocalFileConfiguration implements Configuration {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String filePath;
	
	private File file;
	
	private Properties props = new Properties();
	
	private boolean needFlush;

	@Override
	public void init(Properties props) throws Exception {
		if (PlaceholderUtils.isPlaceholderProperty(filePath)) {
			filePath = props.getProperty(PlaceholderUtils.trimPlaceholder(filePath));
		}
		Assert.isTrue(StringUtils.isNotBlank(filePath), "filePath is required");
		file = new File(filePath);
		if (!file.exists()) {
			//create file
			if (!file.createNewFile()) {
				throw new RuntimeException(String.format("can't create file [%s]", filePath));
			}
		}
		file.setWritable(true);
		if (!file.canWrite()) {
			throw new RuntimeException(String.format("can't write file [%s], plz check", filePath));
		}
		//初始化时 load 文件内容到 props
		load(file);
		Executors.newScheduledThreadPool(
				1, new CustomizableThreadFactory("LocalFileConfigurator-flush-")
			).scheduleWithFixedDelay(
				new FlushTask(file), 60, 120, TimeUnit.SECONDS
			);
	}
	
	private void load(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			props.load(is);
		} catch (Exception e) {
			throw new RuntimeException("load file failed", e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/* (non-Javadoc)
	 * @see com.moreopen.config.agent.LocalConfigurator#set(java.lang.String, java.lang.String)
	 */
	@Override
	public void set(String key, String value) {
		if (value != null && !value.equals(props.get(key))) {
			props.setProperty(key, value);
			needFlush = true;
		}
	}

	/* (non-Javadoc)
	 * @see com.moreopen.config.agent.LocalConfigurator#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		return props.getProperty(key);
	}
	
	public Properties loadAll() {
		return props;
	}
	
	class FlushTask implements Runnable {
		private File file;
		public FlushTask(File file) {
			this.file = file;
		}
		@Override
		public void run() {
			if (needFlush) {
				flush(file);
				needFlush = false;
			}
		}
	}
	
	private void flush(File file) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			props.store(out, null);
		} catch (Exception e) {
			logger.error(String.format("flush to file [%s] failed", filePath), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	//重新设置全量的配置项进 props, flush 时可以全量备份到文件
	@Override
	public void reset(Properties properties) {
		System.out.print("============reset properties : " + properties);
		props.putAll(properties);
		//XXX 直接 flush
		flush(file);
	}

}
