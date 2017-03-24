package com.kuxun.kxtopology.pojo;

import org.slf4j.Logger;

import com.kuxun.kxlog.IDataSource;
import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.IService;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.TimeCacheMap.ExpiredCallback;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.objectassist.FrameLoggerFactory;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.util.JarClassLoader;

/**
 * 卸载class
 */
public class ObjectExpireCallback implements ExpiredCallback {

	final static Logger logger = org.slf4j.LoggerFactory.getLogger(ObjectExpireCallback.class);

	@Override
	public void expire(final Object key, Object val) {

		if (logger.isInfoEnabled()) {

			logger.info("服务 '" + key + "' 过期, 对应的 " + val + " jar对象将被清除，占用的空间将被释放");
		}
		if (val instanceof ObjectRelations) {
			try {
				ObjectRelations relations = (ObjectRelations) val;
				IDataSource dataSource = relations.getDataSource();
				if (dataSource != null)
					dataSource.cleanup();
				IParser parser = relations.getLogParser();
				if (parser != null)
					parser.destory();
				IService service = relations.getLogic();
				if (service != null)
					service.destroy();
				IFilter[] filters = relations.getFilters();
				if (filters != null) {
					for (IFilter filter : filters) {
						filter.destory();
					}
				}
				ClassLoader cl = relations.getClassloader();

				relations = null;

				if (cl instanceof JarClassLoader) {
					((JarClassLoader) cl).release();
				}
				// 移除日志服务
				KxTopologyCacheContext.getSingleCacheContext().remove(FrameLoggerFactory.KEY,
						FrameLoggerFactory.TEMPLATE);

				KxTopologyCacheContext.getSingleCacheContext().remove(FrameLoggerFactory.KEY, FrameLoggerFactory.VALUE,
						String.valueOf(key));

			} catch (KxException kx) {
				if (logger.isErrorEnabled()) {
					logger.error(Utils.stackTrace(kx));
				}
			}

		}
	}
}
