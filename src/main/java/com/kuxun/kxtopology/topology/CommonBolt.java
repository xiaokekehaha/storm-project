package com.kuxun.kxtopology.topology;

import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.cache.MapAware;
import com.kuxun.kxtopology.jarservice.JarAware;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.jarservice.JarService;
import com.kuxun.kxtopology.jarservice.JarTopologyEvent;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;

public abstract class CommonBolt extends GenericBolt implements JarAware {

	final static Logger logger = LoggerFactory.getLogger(CommonBolt.class);

	public CommonBolt(String topologyName) {
		super(topologyName);
	}

	@Override
	public void notifyJarConfig(JarConfig config) {
		// TODO Auto-generated method stub
		if (config == null)
			return;
		// cache.add(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE, config);
	}

	@Override
	public final boolean isAvailale(final String service) {
		// TODO Auto-generated method stub
		// Object obj =
		// KxTopologyCacheContext.getSingleCacheContext().getObject(ObjectHelper.CLASSKEY,
		// ObjectHelper.CLASSVALUE);
		// if (obj == null)
		// return false;

		MapAware mapAware = new MapAware() {
			@Override
			public Object getKey() {
				// TODO Auto-generated method stub
				return service;
			}

			@Override
			public Object getValue() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return KxTopologyCacheContext.getSingleCacheContext().contains(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE,
				mapAware)
				|| KxTopologyCacheContext.getSingleCacheContext().contains(ObjectHelper.CLASSKEY,
						ObjectHelper.WASTECLASS, mapAware);
	}

	@Override
	public final void updateJarInfo(List<Jar> jars) throws KxException {
		// TODO Auto-generated method stub
		String user = getClass().getName();
		if (jars == null)
			return;
		for (Jar jar : jars) {
			super.doChangedJars(jar);
		}
		doSubJob(jars);
	}

	@Override
	void doNewOrUpdate(Jar jar) {
		// 注册Jar文件服务
		String thisJarService = jar.getService();
		JarService.getSingleJarServiceInstance(stormConf).register(
				new JarTopologyEvent(this.getClass()).buildFrequency(JarTopologyEvent.Frequency.ONLYONCE)
						.buildInterval(-1).buildJarAware(this).buildParams(thisJarService, topologyName)
						.buildOperationType(JarService.UPDATE).buildServiceName(thisJarService)
						.enableFileService(enableFileService).version(jar.getVersion()));
	}

	/**
	 * 用于子类完成更细微的操作
	 * 
	 * @param jars
	 */
	abstract void doSubJob(List<Jar> jars) throws KxException;

	@Override
	public void notifyJarChange(List<Jar> changedJars) {
		// TODO Auto-generated method stub

	}

	@Override
	public final void loadService(String service) {
		// 注册Jar文件服务
		JarService.getSingleJarServiceInstance(stormConf).register(
				new JarTopologyEvent(this.getClass()).buildFrequency(JarTopologyEvent.Frequency.ONLYONCE)
						.buildInterval(-1).buildJarAware(this).buildParams(service, topologyName)
						.buildOperationType(JarService.DOWNLOAD).buildServiceName(service)
						.enableFileService(enableFileService));
	}

	@Deprecated
	public ObjectRelations getJarConfigByServiceName(String service) {
		ObjectRelations or = null;
		Object obj = KxTopologyCacheContext.getSingleCacheContext().getObject(ObjectHelper.CLASSKEY,
				ObjectHelper.CLASSVALUE);
		String user = getClass().getName();
		if (obj instanceof Hashtable) {
			Hashtable table = (Hashtable) obj;
			Object value = table.get(service);
			if (value instanceof ObjectRelations)
				or = (ObjectRelations) value;
		}
		if (logger.isInfoEnabled()) {
			logger.info("尝试获取服务'" + service + "'对应的jar对象,对象：" + or + ",若对象为空，可能缓存中还没有缓存该jar对象");
		}
		return or;
	}

	public ObjectRelations getJarRelationsByServiceName(String service) {
		ObjectRelations relations = KxTopologyCacheContext.getSingleCacheContext().getValue(ObjectHelper.CLASSKEY,
				ObjectHelper.CLASSVALUE, ObjectRelations.class, service);
		if (logger.isInfoEnabled()) {
			logger.info("尝试获取服务'" + service + "'对应的jar对象,对象：" + relations + ",若对象为空，可能缓存中还没有缓存该jar对象");
		}
		return relations;
	}

}
