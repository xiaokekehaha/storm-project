package com.kuxun.kxtopology.topology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.cache.MapAware;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 处理发生更新或者删除的jar文件
 */
public abstract class ChangedJar implements IChangedJarAware {

	final static Logger logger = LoggerFactory.getLogger(ChangedJar.class);

	@Override
	public void doChangedJars(Jar jar) {
		// TODO Auto-generated method stub
		int status = jar.getStatus();
		final String thisJarService = jar.getService();
		ObjectRelations or = KxTopologyCacheContext.getSingleCacheContext()
				.getValue(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE, 
						ObjectRelations.class, thisJarService);
		// 将对象从可用表移除
		KxTopologyCacheContext.getSingleCacheContext().remove(ObjectHelper.CLASSKEY, ObjectHelper.CLASSVALUE,
				new MapAware() {
					@Override
					public Object getKey() {
						// TODO Auto-generated method stub
						return thisJarService;
					}

					@Override
					public Object getValue() {
						return null;
					}
				});
		// 将对象加入废弃队列中
		if (or != null) {
			JarConfig jc = or.getConfig();
			//将对应所有的jarConfig 移除
			KxTopologyCacheContext.getSingleCacheContext().remove(jc);
			final ObjectRelations relations = or;
			if(logger.isInfoEnabled()){
			logger.info("尝试将发生 update/delete/deprocated 服务  '" + thisJarService + "' 对应的jar对象移至垃圾堆中.");
			
			}
			KxTopologyCacheContext.getSingleCacheContext().add(ObjectHelper.class, ObjectHelper.WASTECLASS,
					new MapAware<String, ObjectRelations>() {
						@Override
						public String getKey() {
							// TODO Auto-generated method stub
							return thisJarService;
						}

						@Override
						public ObjectRelations getValue() {
							// TODO Auto-generated method stub
							return relations;
						}
					});
		}

		// 可能为新增文件，也有可能是jar文件发生了更新操作
		if (status == Jar.USABLE) {
			 doNewOrUpdate(jar);
		}
	}
	
	/**
	 * 处理发生新增/更新的jar文件
	 * @param jar
	 */
	abstract void doNewOrUpdate(Jar jar);
	
}
