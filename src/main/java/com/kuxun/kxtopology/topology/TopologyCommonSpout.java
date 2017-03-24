package com.kuxun.kxtopology.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import com.kuxun.kxtopology.cache.KxTopologyCacheContext;
import com.kuxun.kxtopology.jarservice.ITopologyUsableJarList;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.jarservice.JarAware;
import com.kuxun.kxtopology.jarservice.JarService;
import com.kuxun.kxtopology.jarservice.JarTopologyEvent;

/**
 * 1. 需要记录在该组件内可用的一些信息
 * 
 * @author dengzh
 */
public abstract class TopologyCommonSpout extends ChangedJar implements IRichSpout, JarAware, ITopologyUsableJarList {

	final static Logger logger = LoggerFactory.getLogger(TopologyCommonSpout.class);

	SpoutOutputCollector _collector;
	TopologySpoutConfig config;
	// KxTopologyCacheContext cache;
	JarService jarService;
	TopologyContext context;
	String topologyName;
	/**
	 * 缓存Class类型
	 */
	final static Class LIST = ArrayList.class;

	public TopologyCommonSpout(TopologySpoutConfig spoutConf, String topologyName) {
		config = spoutConf;
		this.topologyName = topologyName;
	}

	Map conf;

	protected boolean enableFileService;

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		enableFileService = Boolean.parseBoolean(String.valueOf(conf.get("file-service")));
		this._collector = collector;
		this.context = context;
		this.conf = conf;
		jarService = JarService.getSingleJarServiceInstance(conf);
		// cache = KxTopologyCacheContext.getSingleCacheContext();
		jarService.register(getThisJarTopologyParams().enableFileService(enableFileService));

	}

	abstract JarTopologyEvent getThisJarTopologyParams();

	abstract Class getClassKey();

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(config.outputFields));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	@Override
	public final List<Jar> getThisComponentUsablejarList() {
		// get the cached jar list for usable.
		Object obj = KxTopologyCacheContext.getSingleCacheContext().getObject(getClassKey(), LIST);
		if (logger.isInfoEnabled()) {
			String log = obj == null ? "nil" : Arrays.toString(((List<Jar>) obj).toArray());
			logger.info("可用的jar文件列表:%s", log);
		}
		return obj == null ? null : (List<Jar>) obj;
	}

	abstract void copyWithChangedJars(List<Jar> changedJars);

	@Override
	public final void notifyJarChange(List<Jar> changedJars) {
		// 发生改变的jar
		if (changedJars == null || changedJars.size() == 0)
			return;

		String user = this.getClass().getName();

		if (logger.isInfoEnabled()) {
			logger.info(user + "获取到了发生更改的jar文件列表:" + Arrays.toString(changedJars.toArray()));
		}
		KxTopologyCacheContext.getSingleCacheContext().remove(getClassKey(), LIST, changedJars);
		// 将发生改变的对象移除内存中
		int size = changedJars.size();
		for (int i = 0; i < size; i++) {
			Jar jar = changedJars.get(i);
			doChangedJars(jar);
		}
		copyWithChangedJars(changedJars);
	}

	@Override
	void doNewOrUpdate(Jar jar) {
		if (logger.isInfoEnabled()) {
			logger.info("将发生 new/update 操作的jar添加到可用的jar文件队列，该jar对应的服务为%s", jar.getService());
		}
		KxTopologyCacheContext.getSingleCacheContext().add(getClassKey(), LIST, jar);
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

}
