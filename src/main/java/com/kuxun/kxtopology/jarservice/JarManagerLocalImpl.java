package com.kuxun.kxtopology.jarservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxtopology.LocalApp;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;
import com.kuxun.kxtopology.util.TopologyUtils;
import com.kuxun.kxtopology.xml.XmlConfigDefinitionReader;

/**
 * 需要jar文件服务 配置文件从classpath路径下获取或者通过用户指定
 * 
 * @author dengzh
 */
public class JarManagerLocalImpl implements IJarManager {

	final static Logger logger = LoggerFactory.getLogger(JarManagerLocalImpl.class);

	Map conf;

	public JarManagerLocalImpl(Map conf) {
		this.conf = conf;
	}

	/**
	 * 1. 判断内存中是否存在这样的一个对象 2. 若没有,则读取配置，存储配置 3. 返回jar信息
	 * 
	 * @throws KxException
	 */
	@Override
	public List<Jar> getChangableJarListFromJarServer(JarTopologyEvent jarTopology) throws KxException {
		// TODO Auto-generated method stub
		JarConfig config = exists();
		if (config == null)
			config = loadJar(jarTopology, null);
		if (config == null) {
			// 日志
			// 异常
			return null;
		}
		Jar jar = new Jar();
		jar.setService(config.getThisJarService());
		jar.setStatus(Jar.USABLE);
		jar.setVersion(0);
		return new ArrayList(Arrays.asList(jar));
	}

	/**
	 * 运行本地代码
	 * 
	 * @throws KxException
	 */
	@Override
	public JarConfig loadJar(JarTopologyEvent jarTopology, String service) throws KxException {
		// TODO Auto-generated method stub
		// 判断JarTopologyParams的类型
		// 获取本地配置
		JarConfig config = exists();
		if (config != null)
			return config;
		// 获取配置文件
		Object obj = conf.get(LocalApp.CONFIGFILE);
		InputStream is = null;
		if (obj != null) {
			File f = new File(String.valueOf(obj));
			// log 日志
			try {
				is = new FileInputStream(f);
				if (logger.isInfoEnabled()) {
					logger.info("准备从" + String.valueOf(obj) + "文件中读取业务定义");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				if (logger.isErrorEnabled()) {
					logger.error("定义服务的配置文件没有找到，文件路径：" + obj);
				}
			}
		}
		try {
			if (is == null) {
				is = getClass().getClassLoader().getResourceAsStream(IJarManager.CONFIG);
				if (logger.isInfoEnabled()) {
					logger.info("准备从根目录下读取服务定义的config.xml配置文件");
				}

			}

			XmlConfigDefinitionReader reader = new XmlConfigDefinitionReader();

			JarConfig thisJarConfig = reader.parse(is);

			ClassLoader classloader = TopologyUtils.findParentClassLoader();

			String thisJarService = jarTopology.getServiceName();
			// 封装对象
			ObjectRelations relations = new ObjectRelations(thisJarService, thisJarConfig, classloader);
			// 缓存该对象
			KxTopologyCacheContext.getSingleCacheContext().add(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE,
					relations);
			return thisJarConfig;
		} catch (KxException e) {
			if (logger.isErrorEnabled()) {
				logger.error("试图读取本地服务定义时，出现异常，异常信息为：" + e.getMessage());
			}
			throw e;
		}
	}

	private JarConfig exists() {
		Object obj = KxTopologyCacheContext.getSingleCacheContext().getObject(ObjectHelper.CLASSKEY,
				ObjectHelper.CLASSVALUE);
		if (!(obj instanceof Map))
			return null;
		Map map = (Map) obj;
		if (map.size() == 0)
			return null;
		ObjectRelations relations = (ObjectRelations) map.values().toArray()[0];

		return relations.getConfig();

	}

	/**
	 * @param serviceName
	 * @return
	 */
	private JarConfig exists(String serviceName) {
		ObjectRelations relations = KxTopologyCacheContext.getSingleCacheContext().getValue(ObjectHelper.CLASSKEY,
				ObjectHelper.CLASSVALUE, ObjectRelations.class, serviceName);
		return relations == null ? null : relations.getConfig();
	}
}
