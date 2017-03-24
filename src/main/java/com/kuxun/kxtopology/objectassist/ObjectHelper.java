package com.kuxun.kxtopology.objectassist;

import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.IDataSource;
import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IFilterChain;
import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.IService;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.impl.CachedChain;
import com.kuxun.kxlog.utils.TimeCacheMap;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.pojo.JarConfig;

public class ObjectHelper {

	final static Logger logger = LoggerFactory.getLogger(ObjectHelper.class);

	public final static Class CLASSKEY = ObjectHelper.class;

	/**
	 * 存放 key： jar对应的service名， value： jar对应的ObjectRelations
	 */
	public final static Class CLASSVALUE = Hashtable.class;

	/**
	 * 用来容纳失效的Jar信息
	 */
	public final static Class WASTECLASS = TimeCacheMap.class;

	public final static int SPOUT = 0;

	public final static int FILTER = 1;

	public final static int LOGPARSER = 2;

	public final static int SERVICE = 3;

	public final static int LOG = 4;

	/**
	 * 获取配置的实例
	 * 
	 * @param config
	 * @param type
	 * @return
	 */

	public static synchronized <T> T getJarInstance(JarConfig jarConf, Class<T> klass) throws KxException {

		String service = jarConf.getThisJarService();
		ObjectRelations relations = KxTopologyCacheContext.getSingleCacheContext().getValue(CLASSKEY, CLASSVALUE,
				ObjectRelations.class, service);
		if (relations == null) {
			String msg = "没找到服务'" + service + "'的jar定义";
			if (logger.isErrorEnabled()) {
				logger.error(msg);
			}
			throw new KxException(msg);
		}
		// 先初始化日志句柄
		FrameLoggerFactory.enableServiceLogger(jarConf, klass);

		ObjectAssistor assistor = new ObjectAssistor(jarConf);
		ClassLoader classloader = relations.getClassloader();
		Object retObj = null;
		try {
			if (IDataSource.class.isAssignableFrom(klass)) {
				IDataSource dataSource = relations.getDataSource();
				if (dataSource == null && !relations.hasDataSourceInited) {
					dataSource = assistor.getSpout(classloader);
					// dataSource 初始化
					if (dataSource != null) {
						dataSource.init(jarConf.getThisJarDataSource());
					}

					relations.setDataSource(dataSource);
					relations.hasDataSourceInited = true;
				}
				retObj = dataSource;
			} else if (IFilterChain.class.isAssignableFrom(klass)) {
				IFilter[] filters = relations.getFilters();
				if (filters == null && !relations.hasFilterInited) {
					filters = assistor.getIFilters(classloader);
					relations.hasFilterInited = true;
					if (filters != null) {
						relations.setFilters(filters);
						int i = 0;
						for (IFilter filter : filters) {
							filter.init(jarConf.getThisJarFilter()[i++]);
						}
					}
				}
				if (filters != null) {
					CachedChain chain = new CachedChain(filters, service);
					retObj = chain;
				}
			} else if (IParser.class.isAssignableFrom(klass)) {
				IParser parser = relations.getLogParser();
				if (parser == null && !relations.hasParserInited) {
					parser = assistor.getLogParser(classloader);
					// parser初始化
					if (parser != null) {
						parser.init(jarConf.getThisJarLogParser());
					}

					relations.setLogParser(parser);
					relations.hasParserInited = true;
				}
				retObj = parser;
			} else if (IService.class.isAssignableFrom(klass)) {
				IService iservice = relations.getLogic();
				if (iservice == null && !relations.hasServiceInited) {
					iservice = assistor.getIService(classloader);
					// service 初始化
					if (iservice != null) {
						iservice.init(jarConf.getThisJarLogicService());
					}
					relations.setLogic(iservice);
					relations.hasServiceInited = true;

				}
				retObj = iservice;
			}
		} catch (KxException kx) {
			String message = "试图获取服务'" + service + "'定义的" + klass + "失败，异常信息:" + kx.getMessage();
			if (logger.isErrorEnabled()) {
				logger.error(message);
			}
			throw kx;
		}

		return cast(klass, retObj);
	}

	private static <T> T cast(Class<T> klass, Object obj) {
		if (obj == null)
			return null;
		return (T) obj;
	}

}
