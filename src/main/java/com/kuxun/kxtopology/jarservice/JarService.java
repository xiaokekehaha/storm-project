package com.kuxun.kxtopology.jarservice;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * Jar 文件服务， 对外提供 单例 在项目中，Jar 提供两种服务，一是提供发生改变的jar信息列表 二是提供下载，并加载到JVM 内存中
 * 
 * @author dengzh
 */
public class JarService {

	final static Logger logger = LoggerFactory.getLogger(JarService.class);

	public final static Class KEY = JarService.class;

	public final static String GETCHANGEDLIST = "获取变更的jar列表";

	public final static String DOWNLOAD = "下载jar";

	public final static String UPDATE = "更新jar";

	int poolsize = 2;

	private static JarService service;

	ExecutorService threadPool;

	Thread t;

	List<PendingValue> pend;

	int refreshTimeIntervals = 1000 * 30;

	long lastRefreshTime = System.currentTimeMillis();

	IJarManager manager;

	boolean isLocal;

	private JarService(Map conf) {

		JarManagerTmp tmp = JarManagerFactory.createManager(conf);
		// 启动服务
		manager = tmp.manager;

		isLocal = tmp.isLocal;

		startService();
	}

	public static class JarManagerTmp {
		public IJarManager manager;
		public boolean isLocal;
	}

	private void startService() {
		// 不用创建新的 线程
		if (isLocal)
			return;
		threadPool = Executors.newFixedThreadPool(poolsize);
		pend = Collections.synchronizedList(new LinkedList<PendingValue>());
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				List<PendingValue> waste = new LinkedList<PendingValue>();
				while (!Thread.currentThread().isInterrupted()) {
					try {
						doJarQueueQuery(waste, false);
						if (System.currentTimeMillis() - lastRefreshTime > refreshTimeIntervals) {
							doJarQueueQuery(waste, true);
							for (Iterator<PendingValue> iter = waste.iterator(); iter.hasNext();) {
								PendingValue value = iter.next();
								pend.remove(value);
							}
							waste.clear();
							lastRefreshTime = System.currentTimeMillis();
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		});
		t.setDaemon(true);
		t.start();
	}

	private void doJarQueueQuery(List<PendingValue> waste, boolean allTime) {
		int size = pend.size();
		for (int i = 0; i < size; i++) {
			PendingValue value = pend.get(i);
			if (value.key.getFrequency() == JarTopologyEvent.Frequency.ONLYONCE && !allTime) {
				// 查看waste中是否已经包含jarTopology
				if (!waste.contains(value)) {
					waste.add(value);
					doJarTask(value);
				}
			} else if (value.key.getFrequency() == JarTopologyEvent.Frequency.AllTIME && allTime) {
				if (System.currentTimeMillis() - lastRefreshTime > value.key.getTimeIntervals())
					doJarTask(value);
			}

		}
	}

	private void doJarTask(final PendingValue value) {
		// TODO Auto-generated method stub
		Runnable r = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				JarTopologyEvent jarTopology = value.key;
				String operationType = jarTopology.getOperationType();
				// 得到改变的jar列表
				if (GETCHANGEDLIST.equals(operationType)) {
					try {
						List<Jar> changedJarList = manager.getChangableJarListFromJarServer(jarTopology);
						// 通知storm组件做出相应的更新操作
						if (jarTopology.getAware() != null) {
							jarTopology.getAware().notifyJarChange(changedJarList);
						}
					} catch (KxException kx) {
						if (logger.isErrorEnabled()) {
							logger.error("获取jar更新列表出现异常，异常信息：" + kx.getMessage());
						}
					}

				} else if (DOWNLOAD.equals(operationType) || UPDATE.equals(operationType)) {
					// 下载jar文件 加载jar进入JVM内存，返回storm组件一份jar配置
					try {
						JarConfig config = manager.loadJar(jarTopology, operationType);
						value.conf = config;
						for (JarTopologyEvent jtp : value.requests) {
							if (jtp.getAware() != null)
								jtp.getAware().notifyJarConfig(config);
						}
						value.requests.clear();
					} catch (KxException kx) {
						if (logger.isErrorEnabled()) {
							logger.error("试图获取'" + jarTopology.getServiceName() + "'服务定义的配置文件失败");
						}
					}
				}
			}
		};
		threadPool.execute(r);
	}

	public static JarService getSingleJarServiceInstance(Map conf) {

		if (service != null) {
			return service;
		}
		if (service == null)
			synchronized (JarService.class) {
				if (service == null)
					service = new JarService(conf);
			}
		return service;
	}

	class PendingValue {
		JarTopologyEvent key;
		List<JarTopologyEvent> requests = new LinkedList<JarTopologyEvent>();
		volatile JarConfig conf;
		volatile int version;

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof PendingValue)
				return ((PendingValue) obj).key.equals(this.key);
			return false;
		}

		public PendingValue(JarTopologyEvent key) {
			this.key = key;
			this.version = key.getVersion();
		}
	}

	/**
	 * 过程分析： 1. 当来一个请求时，判断是否在队列中包含类似的请求， 1.
	 * 如果包含，则进一步查找返回的请求对应的jar配置是否为空，若jar配置不为空，则将jar配置信息通知请求者 2.
	 * 若上述步骤不包含对应的jar配置，则将该类似的请求添加到请求的队列当中，由线程池通知
	 * 
	 * 2. 请求队列中不包含类似的请求，则创建相应的请求信息，将该请求信息添加到请求队列中。
	 * 
	 * @param params
	 *            请求信息
	 */

	/**
	 * @TODO 在本地模式下，获取变更的jar信息列表和下载更新jar具有相同的作用， 将配置解析加载到内存中，不同的是他们两个返回的结果是不一样的
	 * @param params
	 */

	public void register(JarTopologyEvent params) {
		// 如果不需要jar文件服务
		if (isLocal) {
			// 查看是否已经在该进程中发生这样的操作，如果已经发生则忽略
			if (GETCHANGEDLIST.equals(params.getOperationType())) {
				try {
					List<Jar> jars = manager.getChangableJarListFromJarServer(params);
					if (params.getAware() != null)
						params.getAware().notifyJarChange(jars);
				} catch (KxException kx) {
					if (logger.isErrorEnabled()) {
						logger.error("获取本地服务定义出现异常，异常信息为:" + kx.getMessage());
					}
				}
			} else if (DOWNLOAD.equals(params.getOperationType()) || UPDATE.equals(params.getOperationType())) {
				// 通知相应的改变
				try {
					JarConfig conf = manager.loadJar(params, null);
					if (params.getAware() != null)
						params.getAware().notifyJarConfig(conf);
				} catch (KxException kx) {
					if (logger.isErrorEnabled()) {
						logger.error("获取本地服务定义出现异常，异常信息为:" + kx.getMessage());
					}
				}
			}
			return;
		}
		// 先判断在pend是是否包含有这样的JarTopologyParams
		// 在位于同一个storm组件不同实例都要请求这样的服务时，这样就只有一个实例能够参与执行
		// jar的下载/加载， 获取可变更的
		// 如果存在，则直接返回
		PendingValue value = getValue(params);
		if (value != null) {
			// 如何通知其他的请求者，这是一个问题?
			if (value.version == params.getVersion()) {
				if (logger.isInfoEnabled()){
					logger.info("在队列中已经存在从" + params.getSource() + "针对服务'" + params.getServiceName() + "'的请求,请求类型为："
							+ params.getOperationType());
				}
				if (value.conf != null && params.getAware() != null) {
					params.getAware().notifyJarConfig(value.conf);
					return;
				}
				value.requests.add(params);
				return;
			} else {
				removeOldVersion(value);
			}
		}
		// 不存在，则添加
		// List<JarTopologyParams> request = new ArrayList<JarTopologyParams>();
		value = new PendingValue(params);
		value.requests.add(params);
		pend.add(value);
		if (logger.isInfoEnabled()){
			logger.info("来自" + params.getSource() + "的服务'" + params.getServiceName() + "'请求将被添加到队列中，请求类型为："
					+ params.getOperationType());
		}
	}

	private void removeOldVersion(PendingValue value) {
		pend.remove(value);
	}

	private PendingValue getValue(JarTopologyEvent params) {

		for (PendingValue value : pend) {
			if (params.equals(value.key))
				return value;
		}

		return null;
	}

}
