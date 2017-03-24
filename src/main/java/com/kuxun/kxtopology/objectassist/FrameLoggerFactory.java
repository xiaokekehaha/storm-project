package com.kuxun.kxtopology.objectassist;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.impl.IServiceLoggerConf;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.pojo.JarConfig;
import com.kuxun.kxtopology.util.TopologyUtils;

/**
 * @TODO 对于不同的业务，有个日志开关和日志输出级别
 * @author dengzh
 */
public class FrameLoggerFactory {

	final static Logger logger = LoggerFactory.getLogger(FrameLoggerFactory.class);

	public final static Class KEY = FrameLoggerFactory.class;

	/**
	 * 模板存储
	 */
	public final static Class VALUE = HashMap.class;

	/**
	 * 模板存储
	 */
	public final static Class TEMPLATE = String.class;

	/*
	 * public static Logger getFrameLogger(Object obj){ return logger; }
	 */

	public static Logger getFrameLogger(Class<?> klass) {
		return (Logger) LoggerFactory.getLogger(klass);
	}

	/**
	 * 在storm中，如果存在名称为'service'的句柄， 则调用相应的日志句柄 否则，则生成klass日志句柄
	 * 
	 * 可否程序添加?
	 * 
	 * @param service
	 * @param clazz
	 * @return
	 */

	public static Logger getServiceLogger(String service, Class<?> klass) {
		/**
		 * 检查是否已经被缓存
		 */
		boolean exits = TopologyUtils.isLoggerValid(service);
		if (exits)
			return LoggerFactory.getLogger(service);

		return LoggerFactory.getLogger(klass);
	}

	final static String LOGGERTEMPLATE = "kooxoo.xml";

	final static String MATCHER = "#service#";

	final static String STORMHOME = "#stormhome#";

	/**
	 * 加载日志配置文件
	 * 
	 * @param service
	 */
	public static void loadServiceLoggerConf(String service) {
		try {
			String template = KxTopologyCacheContext.getSingleCacheContext().getValue(FrameLoggerFactory.KEY,
					FrameLoggerFactory.TEMPLATE, String.class, null);
			if (template == null) {
				synchronized (FrameLoggerFactory.class) {
					if (template == null) {
						template = TopologyUtils.loadXmlTemplate(LOGGERTEMPLATE);
						KxTopologyCacheContext.getSingleCacheContext().add(FrameLoggerFactory.KEY,
								FrameLoggerFactory.TEMPLATE, template);
					}
				}

			}
			if (template == null) {
				throw new KxException("未找到相应的日志模板,检查在根目录下是否包含'" + LOGGERTEMPLATE + "'文件");
			}

			template = template.replaceAll(MATCHER, service);
			// template = template.replaceAll(STORMHOME,
			// TopologyUtils.getLogConfiguration());
			byte[] xmlBytes = template.getBytes();
			TopologyUtils.loadServiceLogger(new ByteArrayInputStream(xmlBytes));
			// logger.info("日志句柄'" + service + "'" +
			// TopologyUtils.isLoggerValid(service) + "");

		} catch (Exception e) {
			Utils.wrapperException(e, "service日志配置解析失败");
		}
	}

	/**
	 * 日志管理
	 * 
	 * @param conf
	 * @param klass
	 */
	public static void enableServiceLogger(JarConfig conf, Class<?> klass) {
		// TODO Auto-generated method stub
		IServiceLoggerConf loggerConf = conf.getThisJarLogConfig();
		// 查看内存中是否已经初始化该logger日志
		String thisJarService = conf.getThisJarService();

		boolean contains = KxTopologyCacheContext.getSingleCacheContext().contains(KEY, VALUE, conf);

		if (loggerConf == null) {
			if (!contains) {
				if (logger.isInfoEnabled()) {
					logger.info("添加'" + thisJarService + "'日志句柄");
				}
				loadServiceLoggerConf(thisJarService);
				KxTopologyCacheContext.getSingleCacheContext().add(KEY, VALUE, conf);
			}

			return;
		}

		// 如果关闭日志
		if (!loggerConf.enable()) {
			if (contains) {
				ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) getServiceLogger(thisJarService,
						klass);
				logger.detachAndStopAllAppenders();
				// KxTopologyCacheContext.getSingleCacheContext().remove(KEY,
				// VALUE, conf);
			}
			return;
			// 如果日志开启 且配置了 logger输出
		}
		boolean reload = true;
		if (contains) {
			JarConfig cachedConf = KxTopologyCacheContext.getSingleCacheContext().getValue(KEY, VALUE, JarConfig.class,
					thisJarService);
			// 如果发生更新
			reload = cachedConf.getThisJarConfigFlag() != conf.getThisJarConfigFlag();
		}

		if (reload) {
			// 先加载默认的 日志句柄
			if (!TopologyUtils.isLoggerValid(thisJarService))
				loadServiceLoggerConf(thisJarService);
			// 得到程序自定义的日志文件
			Level level = loggerConf.getLevel();
			ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) getServiceLogger(thisJarService,
					klass);
			if (level != null) {
				logger.setLevel(level);
			}
			Appender appender = loggerConf.getAppender();
			if (appender != null) {
				logger.detachAndStopAllAppenders();
				logger.addAppender(appender);
			}
			KxTopologyCacheContext.getSingleCacheContext().add(KEY, VALUE, conf);
		}
	}

	/*
	 * @Override public void setLogTemplate(String template) { synchronized
	 * (getClass()) { FrameLoggerFactory.template = template; } }
	 */

}
