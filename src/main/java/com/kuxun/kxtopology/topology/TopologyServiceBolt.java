package com.kuxun.kxtopology.topology;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.kuxun.kxlog.IService;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.impl.InputImpl;
import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.objectassist.ObjectHelper;
import com.kuxun.kxtopology.objectassist.ObjectRelations;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 执行具体的业务逻辑
 * 
 * @author dengzh
 */
public class TopologyServiceBolt extends CommonBolt {

	final static Logger logger = LoggerFactory.getLogger(TopologyServiceBolt.class);

	public TopologyServiceBolt(String topologyName) {
		super(topologyName);
	}

	@Override
	public void doTask(String service, Tuple tuple) throws KxException {
		IService thisService = null;
		ObjectRelations relations = getJarRelationsByServiceName(service);
		if (relations == null) {
			if (logger.isInfoEnabled()) {
				logger.info("调用服务'" + service + "'日志解析器", service);
			}
			throw new KxException(service + "对应的jar未找到");
		}
		JarConfig conf = relations.getConfig();
		// Object obj = ObjectHelper.initObjectByName(conf,
		// ObjectHelper.SERVICE);
		thisService = ObjectHelper.getJarInstance(conf, IService.class);
		if (thisService != null) {
			InputImpl input = (InputImpl) tuple.getValue(1);

			Object tupleInfo = input.getParameter("data_transfer_without_parser_and_filter_");
			if (tupleInfo != null) {
				input.setTupleInfo((List) tupleInfo);
			} else {
				input.setTupleInfo(tuple.getValues());
			}
			// 传入storm的配置
			input.putAllTransientValues(stormConf);
			if (logger.isInfoEnabled()) {
				logger.info("开始调用服务'" + service + "'业务逻辑入口", service);
			}
			try {
				thisService.service(input);
			} catch (Exception ex) {
				// @TODO
				String message = "调用服务'" + service + "'有业务逻辑入口时出现异常，异常信息为:" + ex.getMessage();
				if (logger.isErrorEnabled()) {
					logger.error(message);
				}
				throw Utils.wrapperException(ex, message, true);
			}
		}
	}

	@Override
	void doSubJob(List<Jar> jars) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
	}
}
