package com.kuxun.kxtopology.topology;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Values;

import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.jarservice.JarService;
import com.kuxun.kxtopology.jarservice.JarTopologyEvent;
import com.kuxun.kxtopology.pojo.JarConfig;

public class TopologyFileSpout extends TopologyCommonSpout {

	final static Logger logger = LoggerFactory.getLogger(TopologyFileSpout.class);
	/**
	 * 缓存key
	 */
	final static Class KEY = TopologyFileSpout.class;

	transient BlockingQueue<List<Jar>> queue;

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		queue = new LinkedBlockingDeque<List<Jar>>();
	}

	public void nextTuple() {
		while (!queue.isEmpty()) {
			List<Jar> changedJars = queue.poll();
			_collector.emit(new Values(changedJars), System.currentTimeMillis());
		}

	}

	public TopologyFileSpout(TopologySpoutConfig config, String topology) {
		super(config, topology);
	}

	@Override
	JarTopologyEvent getThisJarTopologyParams() {
		return new JarTopologyEvent(this.getClass()).buildFrequency(JarTopologyEvent.Frequency.AllTIME)
				.buildInterval(2000).buildOperationType(JarService.GETCHANGEDLIST).buildTopologyUsableList(this)
				.buildParams(topologyName, "TopologyFileSpout").buildJarAware(this);

	}

	@Override
	Class getClassKey() {
		return KEY;
	}

	@Override
	void copyWithChangedJars(List<Jar> changedJars) {
		// TODO Auto-generated method stub
		queue.add(changedJars);
		if (logger.isInfoEnabled()) {
			logger.info("将变更的jar信息添加到队列中，准备通知拦截节点和服务节点，变更的jar信息:" + changedJars);
		}
	}

	@Override
	public void ack(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyJarConfig(JarConfig config) {
		// TODO Auto-generated method stub

	}

}
