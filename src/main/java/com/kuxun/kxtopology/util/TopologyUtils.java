package com.kuxun.kxtopology.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.common.ReportableException;
import com.kuxun.kxlog.utils.Utils;

public class TopologyUtils {

	final static Logger logger = LoggerFactory.getLogger(TopologyUtils.class);

	public static ClassLoader findParentClassLoader() {
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if (parent == null) {
			parent = TopologyUtils.class.getClassLoader();
			if (parent == null) {
				parent = ClassLoader.getSystemClassLoader();
			}
		}
		return parent;
	}

	// 保存文件
	public static void saveFile(File f, byte[] jarByteArray) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		if (!f.exists())
			f.createNewFile();
		fos.write(jarByteArray);
		fos.flush();
		fos.close();
	}

	public static void reportErrors(SpoutOutputCollector _collector, Throwable throwable, String message) {
		// TODO Auto-generated method stub
		KxException kex = Utils.wrapperException(throwable, message, true);
		_collector.reportError(kex);
	}

	public static void reportErrors(OutputCollector _collector, KxException kx) {
		if (kx instanceof ReportableException)
			_collector.reportError(kx);

	}

	public static String getLogConfiguration() {
		String configuration = System.getProperty("logback.configurationFile");
		String subfix = "/logback/cluster.xml";
		if (configuration.endsWith(subfix)) {
			int slen = subfix.length();
			int len = configuration.length();
			configuration = new String(configuration.substring(0, len - slen));
		}
		return configuration;
	}

	/**
	 * 检查logger name 为 service 的 logger是否合法
	 * 
	 * @param service
	 *            logger name
	 * @return true 存在 logger name 为 @param service 的日志句柄，反之，返回false，表示不存在
	 */
	public static boolean isLoggerValid(String service) {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		return context.exists(service) != null;
	}

	/**
	 * 加载log配置文件
	 * 
	 * @param is
	 */
	public static void loadServiceLogger(InputStream is, boolean reset) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			if (reset)
				context.reset();
			configurator.doConfigure(is);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);

	}

	public static void loadServiceLogger(InputStream is) {
		loadServiceLogger(is, false);
	}

	public static String loadXmlTemplate(String fileName) throws IOException {
		BufferedReader br = null;
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		br = new BufferedReader(new InputStreamReader(is));
		StringBuffer strBuffer = new StringBuffer();
		String spi = "";
		while ((spi = br.readLine()) != null) {
			strBuffer.append(spi).append("\n");
		}

		return new String(strBuffer);
	}

	public static interface NotifyOfXmlTemplate {
		public void setLogTemplate(String template);
	}

	public static void main(String[] args) throws IOException {
		
	}

}
