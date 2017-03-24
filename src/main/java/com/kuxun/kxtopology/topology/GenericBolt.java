package com.kuxun.kxtopology.topology;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxtopology.jarservice.Jar;
import com.kuxun.kxtopology.util.TopologyUtils;

/**
 * @author dengzh
 * 
 *         考虑一般情况，当KafkaSpout传递一条消息或者从过滤节点传递消息到服务节点时 1.取得对应的服务
 *         2.根据服务查看本Bolt是否支持这种服务，如果可以，则调用相应的方法。 3.如果不可以，则注册下载服务，
 * 
 * @TODO 对于项目中的一条日志可以对应多个不同的服务，服务之间以逗号分隔
 * 
 */
public abstract class GenericBolt extends ChangedJar implements IRichBolt {

	final static Logger logger = LoggerFactory.getLogger(GenericBolt.class);

	protected OutputCollector _collector;

	static final String LOGSPLITER = "\t";

	static final String SERVICESPLITER = ",";

	protected String topologyName;

	public GenericBolt(String topologyName) {
		this.topologyName = topologyName;
	}

	protected TopologyContext context;

	protected boolean enableFileService;

	protected transient Map stormConf;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		enableFileService = Boolean.valueOf(String.valueOf(stormConf.get("file-service")));
		this._collector = collector;
		this.context = context;
		this.stormConf = stormConf;
	}

	public void execute(Tuple input) {
		String component = input.getSourceComponent();
		String[] services = null;
		try {
			if (TopologyComponentId.KAFkASPOUT.equals(component)) {
				String log = input.getString(0);
				int index = log.lastIndexOf(LOGSPLITER);
				if (index > -1) {
					services = log.substring(index + 1).split(SERVICESPLITER, -1);
				} else {
					String message = "错误的日志格式类型，原始日志:" + log + ",日志格式：[\\w+\\t]+[service,]+[service]";
					if (logger.isErrorEnabled()) {
						logger.error(message);
					}
					throw new KxException(message);
				}
			} else if (TopologyComponentId.FILESPOUT.equals(component)) {
				// tuple from TopologyFileSpout, notify delete or update event.
				List<Jar> jars = (List<Jar>) input.getValue(0);
				try {
					updateJarInfo(jars);
					return;
				} catch (Exception e) {
					throw new KxException(e, "更新jar文件失败");
				}
			} else {
				services = new String[] { input.getString(0) };
			}

			if (services == null || services.length == 0) {
				String message = "没有找到相应的服务，当前输入信息:" + input.getString(0);
				if (logger.isErrorEnabled()) {
					logger.error(message);
				}
				throw new KxException(message);
			}
			// 如果当前的bolt不支持service
			for (String service : services) {
				if (!isAvailale(service)) {
					String user = this.getClass().getName();
					if (logger.isInfoEnabled()) {
						logger.info("服务'" + service + "'没有找到对应的jar文件，将注册下载服务");
					}
					loadService(service);
				}
				doTask(service, input);
			}
		} catch (KxException kx) {

			TopologyUtils.reportErrors(_collector, kx);

		} finally {

			_collector.ack(input);
		}
	}

	public void cleanup() {
		// TODO Auto-generated method stub
	}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	/**
	 * 加载该业务逻辑
	 * 
	 * @param service
	 */
	public abstract void loadService(String service);

	/**
	 * Bolt 是否支持该业务逻辑
	 */
	public abstract boolean isAvailale(String service);

	/**
	 * 更新jar信息
	 * 
	 * @param url
	 * @param jars
	 * @throws Exception
	 */
	public abstract void updateJarInfo(List<Jar> jars) throws KxException;

	/**
	 * 执行 bolt方法 bolt的主要方法
	 * 
	 * @param tuple
	 * @throws KxException
	 */
	public abstract void doTask(String service, Tuple tuple) throws KxException;

}
