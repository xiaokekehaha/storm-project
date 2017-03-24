package com.kuxun.kxtopology.objectassist;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.IParserConfig;
import com.kuxun.kxlog.IService;
import com.kuxun.kxlog.IServiceConfig;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.pojo.JarConfig;

public class ObjectAssistor {

	/**
	 * 配置文件
	 */
	JarConfig config;

	String service;

	public ObjectAssistor(JarConfig conf) {
		this.config = conf;
		service = conf.getThisJarService();
	}

	/**
	 * 得到日志解析器实例
	 * 
	 * @param classLoader
	 *            当前ClassLoader
	 * @return 日志解析器
	 * @throws ClassNotFoundException
	 * @throws LinkageError
	 */
	public IParser getLogParser(ClassLoader classLoader) throws KxException {
		IParserConfig conf = config.getThisJarLogParser();
		try {
			if (conf == null)
				return null;
			String parserClass = conf.getThisLogParserClassName();
			IParser parser = null;
			if (Utils.isNotBlank(parserClass)) {
				Class<?> clazz = BeanUtils.forName(parserClass, classLoader);
				parser = (IParser) BeanUtils.instantiate(clazz);
			}
			return parser;
		} catch (Exception e) {
			//e.printStackTrace();
			throw Utils.wrapperException(e, "创建'" + service + "'对应的日志解析器失败,class:" + conf.getThisLogParserClassName(),
					true);
		}
	}

	/**
	 * 得到业务逻辑对应的过滤链
	 * 
	 * @param classLoader
	 * @return 过滤链
	 * @throws ClassNotFoundException
	 * @throws LinkageError
	 */
	public IFilter[] getIFilters(ClassLoader classLoader) throws KxException {
		String klass = "";
		try {
			IFilterConfig[] filterConfigs = config.getThisJarFilter();
			if (filterConfigs == null || filterConfigs.length == 0)
				return null;
			int len = filterConfigs.length;
			IFilter[] filters = new IFilter[len];
			for (int i = 0; i < len; i++) {
				IFilterConfig config = filterConfigs[i];
				klass = config.getThisFilterClassName();
				IFilter thisFilter = null;
				if (Utils.isNotBlank(klass)) {
					Class<?> clazz = BeanUtils.forName(klass, classLoader);
					thisFilter = (IFilter) BeanUtils.instantiate(clazz);
					filters[i] = thisFilter;
				}
			}
			return filters;
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw Utils.wrapperException(ex, "创建'" + service + "'对应的过滤器失败,class:" + klass, true);
		}
	}

	/**
	 * 得到业务逻辑入口
	 * 
	 * @param classloader
	 * @return 业务服务
	 * @throws ClassNotFoundException
	 * @throws LinkageError
	 */
	public IService getIService(ClassLoader classloader) throws KxException {
		String klass = "";
		try {
			IServiceConfig serviceConfig = config.getThisJarLogicService();
			if (serviceConfig == null)
				return null;
			IService is = null;
			klass = serviceConfig.getThisServiceClassName();
			if (Utils.isNotBlank(klass)) {
				Class<?> clazz = BeanUtils.forName(klass, classloader);
				is = (IService) BeanUtils.instantiate(clazz);
			}

			return is;
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw Utils.wrapperException(ex, "创建'" + service + "'对应的服务入口失败,class:" + klass, true);
		}
	}

	/**
	 * 得到业务数据发射源
	 * 
	 * @param classloader
	 * @return 业务数据发射源
	 * @throws ClassNotFoundException
	 * @throws LinkageError
	 */
	public com.kuxun.kxlog.IDataSource getSpout(ClassLoader classloader) throws KxException {
		String klass = "";
		try {
			com.kuxun.kxlog.IDataSource dataSource = null;
			IDataSourceConfig sourceConfig = config.getThisJarDataSource();
			if (sourceConfig == null)
				return null;
			klass = sourceConfig.getThisDataSourceClassName();
			if (Utils.isNotBlank(klass)) {
				Class<?> clazz = BeanUtils.forName(klass, classloader);
				dataSource = (com.kuxun.kxlog.IDataSource) BeanUtils.instantiate(clazz);
			}
			return dataSource;
		} catch (Exception ex) {
			//ex.printStackTrace();
			throw Utils.wrapperException(ex, "创建'" + service + "'对应的数据源失败,class:" + klass, true);
		}
	}

}
