package com.kuxun.kxtopology.topology;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Values;

import com.kuxun.kxlog.IDataSource;
import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.common.DataSourceStatus;
import com.kuxun.kxlog.common.IDataSourceStatusAware;
import com.kuxun.kxlog.impl.ContextImpl;
import com.kuxun.kxlog.impl.InputImpl;
import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.jarservice.JarService;
import com.kuxun.kxtopology.jarservice.JarTopologyEvent;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.pojo.JarConfig;
import com.kuxun.kxtopology.util.TopologyUtils;

/**
 * 自定义发射数据源 1. 自定义数据源并发度 2. 统计不同业务数据源的成功率和失败率 3. 统计正在运行的业务 考虑使用zookeeper来实现这类功能
 * 考虑一个Spout的生命周期
 * 
 * @author dengzh
 */
public class TopologyDataSource extends TopologyCommonSpout {

	final static Logger logger = LoggerFactory.getLogger(TopologyDataSource.class);
	/**
	 * 缓存key
	 */
	final static Class KEY = TopologyDataSource.class;

	/**
	 * 用来缓存数据发射源， 对于一个Spout来说，可能对应多个数据发射源，因而使用HashMap key: service, value:
	 * JarConfig 对于这一类存储来说，这影响的是所有这个类的实例
	 * 
	 */
	final static Class VALUE = HashMap.class;

	/**
	 * 存放本实例所有数据发射源实例
	 */
	Map<String, SpoutConf> dataSources = new ConcurrentHashMap<String, SpoutConf>();

	public class SpoutConf {
		public IDataSource dataSource;
		// 增加标识信息，用已区分是否发生更新操作
		public JarConfig conf;
		long thisDataSourceFlag;
		ContextImpl ctx = new ContextImpl(TopologyDataSource.this.conf);
	}

	TopologySpoutConfig dataSourceConf;

	public TopologyDataSource(TopologySpoutConfig dataSourceConf, String topology) {
		super(dataSourceConf, topology);
	}

	@Override
	JarTopologyEvent getThisJarTopologyParams() {
		return new JarTopologyEvent(this.getClass()).buildFrequency(JarTopologyEvent.Frequency.AllTIME)
				.buildInterval(2000).buildTopologyUsableList(this).buildParams(topologyName, "datasource")
				.buildOperationType(JarService.GETCHANGEDLIST).buildJarAware(this);
	}

	@Override
	public Class getClassKey() {
		// TODO Auto-generated method stub
		return KEY;
	}

	DataSourceCoordinator coordinator;

	// ZkState _state;

	// transient ExecutorInfMetric executorInf;

	// transient CountMetric countMetric;

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		coordinator = new DataSourceCoordinator(context);
		// executorInf = new ExecutorInfMetric();
		// context.registerMetric("executorinf", executorInf, 10);
		// context.registerMetric("count", countMetric = new CountMetric(), 15);
	}

	@Override
	public void nextTuple() {
		// 得到缓存的自定义数据源
		// 在这一步中将本实例与该JVM进程中存放的数据进行信息匹配
		// 剔除本实例中 已被删除/废弃的数据源
		// 添加新增的数据源，如何区分更新的数据源?
		long start = System.currentTimeMillis();
		Object obj = KxTopologyCacheContext.getSingleCacheContext().getObject(KEY, VALUE);
		Map<String, JarConfig> cachedJarConfig = null;
		if (obj != null)
			cachedJarConfig = (Map<String, JarConfig>) obj;
		else {
			cachedJarConfig = new HashMap<String, JarConfig>();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/**
		 * 1. cachedJarConfig 有服务，而dataSources中没有，标识新增加的服务，初始化该数据源并加入本实例中 2.
		 * cachedJarConfig 无服务，而dataSources中有， 则标识剔除的服务，删除dataSources中对应的服务 3.
		 * cachedJarConfig 有服务，dataSources也包含该服务，判断是否发生更新操作，若是，则移除原来的数据源实例
		 * 并添加新初始化的该数据源实例。
		 */
		Collection<String> cachedKeys = cachedJarConfig.keySet();
		for (String key : cachedKeys) {
			JarConfig config = cachedJarConfig.get(key);
			if (dataSources.containsKey(key)) {
				// 第三种情况
				SpoutConf value = dataSources.get(key);
				// 发生更新
				if (config.getThisJarConfigFlag() != value.thisDataSourceFlag) {
					// 更新操作-----》
					// Object thisObj = ObjectHelper.initObjectByName(config,
					// ObjectHelper.SPOUT);
					try {
						IDataSource dataSource = ObjectHelper.getJarInstance(config, IDataSource.class);
						if (dataSource != null) {
							value.dataSource = dataSource;
							value.thisDataSourceFlag = config.getThisJarConfigFlag();
							value.conf = config;
							int size = context.getComponentTasks(context.getThisComponentId()).size();
							int parallelism = config.getThisJarDataSource().getThisDataSourceParallelism();
							if (size > parallelism)
								size = parallelism;
							value.ctx.setTotalTask(size);
						}
					} catch (Exception e) {
						String message = "更新服务'" + key + "'定义的数据源出现异常，异常信息为:" + e.getMessage();
						if (logger.isErrorEnabled()) {
							logger.error(message);
						}
						TopologyUtils.reportErrors(_collector, e, message);
					}
				}
			} else {
				// @TODO 第一种情况
				// 新添加一个数据发射源
				// Object thisObj = ObjectHelper.initObjectByName(config,
				// ObjectHelper.SPOUT);
				try {
					IDataSource dataSource = ObjectHelper.getJarInstance(config, IDataSource.class);
					if (dataSource != null) {
						SpoutConf value = new SpoutConf();
						value.dataSource = dataSource;
						value.thisDataSourceFlag = config.getThisJarConfigFlag();
						value.conf = config;
						value.ctx.setSourceComponent(context.getThisComponentId());
						value.ctx.setTaskIndex(context.getThisTaskIndex());
						int size = context.getComponentTasks(context.getThisComponentId()).size();
						int parallelism = config.getThisJarDataSource().getThisDataSourceParallelism();
						if (size > parallelism)
							size = parallelism;
						value.ctx.setTotalTask(size);
						dataSources.put(key, value);
					}
				} catch (Exception e) {
					String message = "获取服务'" + key + "'定义的数据源出现异常，异常信息为:" + e.getMessage();
					if (logger.isErrorEnabled()) {
						logger.error(message);
					}
					TopologyUtils.reportErrors(_collector, e, message);
				}
			}
		}
		cachedKeys = dataSources.keySet();
		// 第二种情况
		for (String key : cachedKeys) {
			if (!cachedJarConfig.containsKey(key)) {
				// 删除剔除的自定义数据源
				dataSources.remove(key);
			}
		}

		// 获取属于本实例的SpoutConf
		List<SpoutConf> spouts = coordinator.calculateSpoutForTask(dataSources);
		// 准备发射数据
		for (SpoutConf entry : spouts) {
			IDataSource dataSource = entry.dataSource;
			DataSourceStatus status = null;
			if (dataSource instanceof IDataSourceStatusAware) {
				status = ((IDataSourceStatusAware) dataSource).getThisDataSourceStatus();
			}
			if (status == DataSourceStatus.DEPROCATED) {
				// @TODO 请求终止该服务，将该服务变成不可用

			}
			if (status == null) {
				status = DataSourceStatus.ACTIVE;
			}
			// 如果是可用的状态
			if (status == DataSourceStatus.ACTIVE) {
				// 发射数据
				emit(entry.conf.getThisJarService(), entry);
			}
		}
	}

	/**
	 * 发射数据时，调用的窗口
	 * 
	 * @param serviceName
	 * @param dataSource
	 */
	private void emit(String serviceName, SpoutConf spout) {
		IDataSource dataSource = spout.dataSource;
		InputImpl input = new InputImpl();
		// 获取该业务对应的上下文环境
		ContextImpl ctx = spout.ctx;
		input.setContext(ctx);
		MsgId msgId = new MsgId(serviceName, System.currentTimeMillis());
		try {
			Object obj = dataSource.message(input, msgId);
			if (obj != null)
				_collector.emit(new Values(serviceName, input), msgId);
			else
				_collector.emit(new Values(serviceName, input));
		} catch (Exception e) {
			String message = "数据源调用'" + serviceName + "'定义的接口生产消息时发生异常,异常信息为:" + e.getMessage();
			if (logger.isErrorEnabled()) {
				logger.error(message);
			}
			TopologyUtils.reportErrors(_collector, e, message);
		}

		// countMetric.incr(serviceName);
	}

	public class MsgId {
		String serviceName;
		long generateTime;

		public MsgId(String serviceName, long generateTime) {
			this.serviceName = serviceName;
			this.generateTime = generateTime;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj)
				return true;
			if (obj instanceof MsgId) {
				MsgId other = (MsgId) obj;
				return serviceName.equals(other.serviceName) && other.generateTime == generateTime;
			}
			return false;
		}

	}

	/**
	 * 如何处理发生改变的jar 1 从本身的队列中移除
	 */
	@Override
	void copyWithChangedJars(List<Jar> changedJars) {
		// 得到缓存的 自定义数据源
		String user = this.getClass().getName();
		Object obj = KxTopologyCacheContext.getSingleCacheContext().getObject(KEY, VALUE);
		if (obj == null)
			obj = KxTopologyCacheContext.getSingleCacheContext().ensure(KEY, VALUE);
		Map<String, JarConfig> cachedJarConfig = (Map<String, JarConfig>) obj;
		if (logger.isInfoEnabled()) {
			logger.info("在获取变更的jar文件之前，组件可用的数据源对应的服务有：" + Arrays.toString(cachedJarConfig.keySet().toArray()));

		}
		for (Iterator<Jar> iter = changedJars.iterator(); iter.hasNext();) {
			Jar jar = iter.next();
			// 判断该jar是否删除或者废弃
			int status = jar.getStatus();
			String service = jar.getService();
			if (status == Jar.DELETE || status == Jar.DEPROCATED) {
				// 从缓存中移除该jar
				cachedJarConfig.remove(service);
				if (logger.isInfoEnabled()) {
					logger.info(user + " 组件移除已经废弃或者删除的数据源，服务为：" + service);
				}
			} else {
				// 如果jar发生更新(新增或者更新),则通知jar文件服务下载jar包
				String operationType = JarService.DOWNLOAD;
				if (cachedJarConfig.containsKey(service)) {
					// 更新操作
					operationType = JarService.UPDATE;

				}
				// 注册Jar文件服务
				jarService.register(new JarTopologyEvent(this.getClass())
						.buildFrequency(JarTopologyEvent.Frequency.ONLYONCE).buildParams(service, topologyName)
						.buildOperationType(operationType).buildJarAware(this).buildInterval(-1)
						.buildServiceName(service).enableFileService(enableFileService).version(jar.getVersion()));
			}
		}

	}

	/**
	 * 得到下载jar的配置
	 */
	@Override
	public void notifyJarConfig(JarConfig config) {

		if (config == null)
			return;
		// 下载jar文件后，可以得到一份Jar文件配置，
		/**
		 * 1. Jar 发生更新，则发生替换 2. Jar 新增，则Map新增一条记录
		 */
		// 从配置文件中得到自定义数据发射源
		IDataSourceConfig sourceConfig = config.getThisJarDataSource();
		// 如果存在，则加入缓存中
		if (sourceConfig != null) {
			// 缓存这份配置
			// 如果数据源发生更新
			// if(cache.contains(KEY, VALUE, config)){
			//
			// }
			if (logger.isInfoEnabled()) {
				logger.info("将服务" + config.getKey() + "对应的自定义数据源添加到" + getClass().getName() + "组件缓存中");
			}
			KxTopologyCacheContext.getSingleCacheContext().add(KEY, VALUE, config);
		}
	}

	@Override
	public void ack(Object arg0) {
		// TODO Auto-generated method stub
		MsgId msgId = null;
		if (arg0 instanceof MsgId)
			msgId = (MsgId) arg0;
		if (msgId == null)
			return;
		// add this info to the zookeeper
		// executorInf.incr(msgId.serviceName, true);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		// _state.close();
	}

	@Override
	public void fail(Object arg0) {
		// TODO Auto-generated method stub
		MsgId msgId = null;
		if (arg0 instanceof MsgId)
			msgId = (MsgId) arg0;
		if (msgId == null)
			return;
		// executorInf.incr(msgId.serviceName, false);
	}

}
