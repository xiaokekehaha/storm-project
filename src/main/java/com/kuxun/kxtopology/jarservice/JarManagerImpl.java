package com.kuxun.kxtopology.jarservice;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.common.ReportableException;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.cache.MapAware;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;
import com.kuxun.kxtopology.resources.IResourceObtainer;
import com.kuxun.kxtopology.resources.Resources;
import com.kuxun.kxtopology.util.HttpUtils;
import com.kuxun.kxtopology.util.JarClassLoader;
import com.kuxun.kxtopology.util.TopologyUtils;
import com.kuxun.kxtopology.xml.XmlConfigDefinitionReader;

/**
 * @author dengzh
 */
public class JarManagerImpl implements IJarManager {

	final static Logger logger = LoggerFactory.getLogger(JarManagerImpl.class);

	IResourceObtainer obtainer = Resources.getSingleInstance();

	public JarManagerImpl() {
	}

	@Override
	public List<Jar> getChangableJarListFromJarServer(JarTopologyEvent params) throws KxException {
		String resourceUrl = obtainer.getJarChangedListAddr(params.getParams());
		if (logger.isInfoEnabled()) {
			logger.info("即将从服务器上获取变更的jar文件列表, 请求来自" + params.getSource() + "，使用url:" + resourceUrl);
		}
		ITopologyUsableJarList usableList = params.getUsableJarList();
		List<Jar> usableJar = null;
		if (usableList != null)
			usableJar = usableList.getThisComponentUsablejarList();

		String postBody = com.alibaba.fastjson.JSONObject.toJSONString(usableJar);
		List<Jar> array = null;
		try {
			String changedList = HttpUtils.post(resourceUrl, "usable", postBody);
			array = JSONArray.parseArray(changedList, Jar.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			String message = "使用地址 " + resourceUrl + " 尝试获取发生变更的jar文件时，发生异常, 异常信息为:" + e.getMessage();
			if (logger.isErrorEnabled()) {
				logger.error(message);
			}
			throw Utils.wrapperException(e, message);
		}
		return array;
	}

	/**
	 * 下载jar文件并将jar文件装载到内存中
	 */
	@Override
	public JarConfig loadJar(JarTopologyEvent jarTopology, String operationType) throws KxException {
		/**
		 * 1. 如果service为download，这意味着这个新增加的服务，对于这种情况，首先判断内存中是否已经包含该jar，若包含，则找出，
		 * 返回相应的JarConfig 如果不存在，则执行 下载 --》 加载 ---》封装---》缓存的操作 2.
		 * 如果service为update，则执行下载---》加载---》封装 ---》缓存的操作
		 */
		final String user = jarTopology.getSource();

		final String thisJarService = jarTopology.getServiceName();

		MapAware<String, ObjectRelations> thisInstance = new MapAware<String, ObjectRelations>() {
			@Override
			public String getKey() {
				// TODO Auto-generated method stub
				return thisJarService;
			}

			@Override
			public ObjectRelations getValue() {
				return null;
			}
		};
		// 判断缓存中是否已经存在该实例
		boolean exits = JarService.DOWNLOAD.equals(operationType)
				&& KxTopologyCacheContext.getSingleCacheContext().contains(ObjectHelper.CLASSKEY,
						ObjectHelper.CLASSVALUE, thisInstance);

		if (exits) {
			Map<String, ObjectRelations> thisServiceMap = (Map<String, ObjectRelations>) KxTopologyCacheContext
					.getSingleCacheContext().getObject(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE);
			ObjectRelations relations = thisServiceMap.get(thisInstance.getKey());
			if (relations != null) {
				if (logger.isInfoEnabled()) {
					logger.info("服务'" + thisJarService + "'对应的jar文件时，发现jar对象已经被缓存" + ", 此时没有必要文件服务中下载该jar文件");
				}
				return relations.getConfig();
			}
		}
		/**
		 * 剩余情况 1. relations == null 2. exits = false; 3. service =
		 * JarService.UPDATE;
		 */
		JarConfig thisJarConfig = null;
		String queryUrl = obtainer.getJarResourceAddr(jarTopology.getParams());

		if (logger.isInfoEnabled()) {
			logger.info(user + "将下载服务" + thisJarService + "对应的jar文件,使用的地址:" + queryUrl);
		}
		try {
			byte[] byteArr = HttpUtils.downloadFile(queryUrl);
			// 加载 jar
			JarClassLoader classloader = new JarClassLoader(new ByteArrayInputStream(byteArr),
					TopologyUtils.findParentClassLoader(), false);
			// 得到配置文件
			byte[] thisJarConfigFile = classloader.getResourceAsByte(CONFIG);
			// 此时该jar文件已经失效，或被删除
			if (thisJarConfigFile == null || thisJarConfigFile.length == 0) {
				String message = "服务'" + thisJarService + "'对应的jar文件为空";
				ReportableException reportEx = new ReportableException(message);
				throw reportEx;
			}
			XmlConfigDefinitionReader reader = new XmlConfigDefinitionReader();
			thisJarConfig = reader.parse(thisJarConfigFile);
			// 封装对象
			ObjectRelations relations = new ObjectRelations(thisJarService, thisJarConfig, classloader);
			relations.setVersion(jarTopology.getVersion());
			// 缓存该对象
			KxTopologyCacheContext.getSingleCacheContext().add(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE,
					relations);
			List<String> cachedService = null;

			Hashtable table = KxTopologyCacheContext.getSingleCacheContext().getObject(ObjectHelper.CLASSKEY,
					Hashtable.class);
			/*
			 * Object obj =
			 * KxTopologyCacheContext.getSingleCacheContext().getObject
			 * (ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE);
			 */
			if (table != null) {
				cachedService = new ArrayList(table.keySet());
			}

			if (logger.isInfoEnabled()) {

				logger.info("服务 " + thisJarService + " 对应的jar文件已经被解析缓存，到目前为止," + user + "所在的进程已经缓存了" + ": "
						+ cachedService + "服务，请求类型为" + operationType);
			}
		} catch (Exception e) {
			String errorMsg = "获取服务" + thisJarService + "对应的jar文件发生异常";
			if (logger.isErrorEnabled()) {
				logger.error(errorMsg);
			}
			throw Utils.wrapperException(e, errorMsg);
		}

		return thisJarConfig;
	}
}
