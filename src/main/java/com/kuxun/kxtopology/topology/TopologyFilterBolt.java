package com.kuxun.kxtopology.topology;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.kuxun.kxlog.Finishable;
import com.kuxun.kxlog.IFilterChain;
import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.impl.InputImpl;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * @author dengzh
 */
public class TopologyFilterBolt extends CommonBolt {

	final static Logger logger = LoggerFactory.getLogger(TopologyFilterBolt.class);

	public TopologyFilterBolt(String topologyName) {
		super(topologyName);
	}

	final static String[] outputFields = { "service", "input" };

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields(outputFields));
	}

	@Override
	public void doTask(String service, Tuple tuple) throws KxException {
		// TODO Auto-generated method stub
		String component = tuple.getSourceComponent();

		ObjectRelations relations = getJarRelationsByServiceName(service);
		if (relations == null) {
			if (logger.isInfoEnabled()) {
				logger.info("当'" + getClass() + "'获取服务'" + service + "'时，得到一个值为空的关系");
			}
			throw new KxException(service + "对应的jar未找到");
		}

		JarConfig conf = relations.getConfig();

		IParser parser = ObjectHelper.getJarInstance(conf, IParser.class);

		IFilterChain filterChain = ObjectHelper.getJarInstance(conf, IFilterChain.class);

		/*
		 * Object obj = ObjectHelper.initObjectByName(conf,
		 * ObjectHelper.LOGPARSER);
		 * 
		 * if (obj instanceof IParser) parser = (IParser) obj;
		 * 
		 * obj = ObjectHelper.initObjectByName(conf, ObjectHelper.FILTER);
		 * 
		 * if (obj instanceof IFilterChain)
		 * 
		 * filterChain = (IFilterChain) obj;
		 */

		// 得到用户的Filter和Parser以后，实现相应的功能
		// 创建上下文环境
		InputImpl input = null;

		if (TopologyComponentId.KAFkASPOUT.equals(component)) {
			// tuple 来源于 kafkaspout
			input = new InputImpl();
			/*
			 * ContextImpl ctx = new ContextImpl();
			 * ctx.setTaskIndex(context.getThisTaskIndex());
			 * ctx.setTotalTask(context
			 * .getComponentTasks(context.getThisComponentId()).size());
			 * ctx.setSourceComponent(context.getThisComponentId());
			 * input.setContext(ctx);
			 */
		} else if (TopologyComponentId.DATASOURCESPOUT.equals(component)) {
			input = (InputImpl) tuple.getValue(1);
		}

		// 将当前的输入设置进request对象中
		input.setTupleInfo(tuple.getValues());
		input.putAllTransientValues(stormConf);

		// 当这条日志对应的服务没有解析器和过滤器这两种角色时，将tuple的消息封装

		if (parser == null && filterChain == null) {

			input.storePermanentData("data_transfer_without_parser_and_filter_", tuple.getValues());
		}

		try {
			if (parser != null) {
				if (logger.isInfoEnabled()) {
					logger.info("调用服务'" + service + "'日志解析器", service);
				}
				parser.doParse(input);
			}
			if (filterChain != null) {
				if (logger.isInfoEnabled()) {
					logger.info("调用服务'" + service + "'过滤链", service);
				}
				filterChain.doFilter(input);
			}

			// 当这条消息通过过滤时，才将该消息发送到下一个节点
			boolean emitable = false;
			if (filterChain == null) {
				emitable = true;
			} else if (filterChain instanceof Finishable) {
				if (((Finishable) filterChain).finished()) {
					emitable = true;
				}
			} else {
				emitable = true;
			}

			if (emitable) {
				_collector.emit(tuple, new Values(service, input));
			}
		} catch (Exception ex) {
			// @TODO
			String message = "调用服务'" + service + "'解析日志/过滤器时发生异常，异常信息：" + ex.getMessage();
			if (logger.isErrorEnabled()) {
				logger.error(message);
			}
			throw Utils.wrapperException(ex, message, true);
		}
	}

	@Override
	public void doSubJob(List<Jar> jars) {
		// TODO Auto-generated method stub

	}

}
