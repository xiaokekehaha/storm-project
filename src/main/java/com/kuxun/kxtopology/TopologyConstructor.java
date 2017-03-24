package com.kuxun.kxtopology;

import java.util.UUID;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

import com.kuxun.kxtopology.topology.TopologyComponentId;
import com.kuxun.kxtopology.topology.TopologyDataSource;
import com.kuxun.kxtopology.topology.TopologyFileSpout;
import com.kuxun.kxtopology.topology.TopologyFilterBolt;
import com.kuxun.kxtopology.topology.TopologyServiceBolt;
import com.kuxun.kxtopology.topology.TopologySpoutConfig;

/**
 * @author dengzh 创建Topology
 * @To do 文件系统属性从外部读入
 * @To do
 */
public class TopologyConstructor {
	final static TopologyBuilder builder = new TopologyBuilder();

	/**
	 * @param zkConnString
	 *            list of Kafka brokers
	 * @param topicName
	 *            topic to read from
	 * @param zkRoot
	 *            the root path in Zookeeper for the spout to store the consumer
	 *            offsets
	 * @param name
	 *            topology name SpoutConfig has options for adjusting how the
	 *            spout fetches messages from Kafka (buffer sizes, amount of
	 *            messages to fetch at a time, timeouts, etc.).
	 * @throws InvalidTopologyException
	 * @throws AlreadyAliveException
	 *             组建 topology
	 */

	public static StormTopology construct(Conf conf, Config config) throws AlreadyAliveException,
			InvalidTopologyException {

		BrokerHosts hosts = new ZkHosts(conf.zookeeper);
		SpoutConfig spoutConfig = new SpoutConfig(hosts, conf.topic, "/" + conf.topic, UUID.randomUUID().toString());
		spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

		builder.setSpout(TopologyComponentId.KAFkASPOUT, kafkaSpout, conf.kafkaSpoutNum);

		TopologySpoutConfig fileSpoutConf = new TopologySpoutConfig(conf.topologyName, new String[] { "jars" });
		TopologyFileSpout fileSpout = new TopologyFileSpout(fileSpoutConf,conf.topologyName);

		builder.setSpout(TopologyComponentId.FILESPOUT, fileSpout, conf.fileSpoutNum);

		TopologySpoutConfig dataSourceConf = new TopologySpoutConfig(conf.topologyName, new String[] { "service",
				"input" });

		dataSourceConf.zk = conf.zookeeper;
		
		TopologyDataSource dataSource = new TopologyDataSource(dataSourceConf,conf.topologyName);
		
		builder.setSpout(TopologyComponentId.DATASOURCESPOUT, dataSource, conf.dataSourceNum);

		builder.setBolt(TopologyComponentId.FILTER, new TopologyFilterBolt(conf.topologyName), conf.filterNum)
				.shuffleGrouping(TopologyComponentId.KAFkASPOUT).shuffleGrouping(TopologyComponentId.DATASOURCESPOUT)
				.allGrouping(TopologyComponentId.FILESPOUT);
		;
		builder.setBolt(TopologyComponentId.SERVICE, new TopologyServiceBolt(conf.topologyName), conf.serviceNum)
				.shuffleGrouping(TopologyComponentId.FILTER).allGrouping(TopologyComponentId.FILESPOUT);

		config.setDebug(true);
		config.setNumWorkers(conf.workerNum);
		
		//是否支持文件服务
	 
	    config.put("file-service", conf.enableFileService);
	    
		return builder.createTopology();
	}

	/**
	 * build a test topology
	 * 
	 * @throws InvalidTopologyException
	 * @throws AlreadyAliveException
	 */
	public static StormTopology constructTest(Conf conf, Config config) throws AlreadyAliveException,
			InvalidTopologyException {

		TopologySpoutConfig fileSpoutConf = new TopologySpoutConfig(conf.topologyName, new String[] { "jars" });

		TopologyFileSpout fileSpout = new TopologyFileSpout(fileSpoutConf,conf.topologyName);

		builder.setSpout(TopologyComponentId.FILESPOUT, fileSpout, conf.fileSpoutNum);

		TopologySpoutConfig dataSourceConf = new TopologySpoutConfig(conf.topologyName, new String[] { "service",
				"input" });

		TopologyDataSource dataSource = new TopologyDataSource(dataSourceConf,conf.topologyName);
		builder.setSpout(TopologyComponentId.DATASOURCESPOUT, dataSource, conf.dataSourceNum);

		builder.setBolt(TopologyComponentId.FILTER, new TopologyFilterBolt(conf.topologyName), conf.filterNum)
				.shuffleGrouping(TopologyComponentId.DATASOURCESPOUT).allGrouping(TopologyComponentId.FILESPOUT);

		builder.setBolt(TopologyComponentId.SERVICE, new TopologyServiceBolt(conf.topologyName), conf.serviceNum)
				.shuffleGrouping(TopologyComponentId.FILTER).allGrouping(TopologyComponentId.FILESPOUT);

		config.setDebug(true);
		config.setNumWorkers(conf.workerNum);

		config.put("file-service", conf.enableFileService);
		
		return builder.createTopology();
	}

	public static int constructTopologyTest(Conf conf, boolean local) throws AlreadyAliveException,
			InvalidTopologyException {
		Config config = new Config();
		StormTopology topology = constructTest(conf, config);
		//if(conf.enableMetrics)
			//   config.registerMetricsConsumer(ExecutorMetricConsumer.class);
		if (local) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(conf.topologyName, config, topology);
			return 0;
		}
		
		StormSubmitter.submitTopology(conf.topologyName, config, topology);
		return 0;

	}

	public static int constructTopology(Conf conf, boolean local) throws AlreadyAliveException,
			InvalidTopologyException {
		Config config = new Config();
		StormTopology topology = construct(conf, config);
	//	if(conf.enableMetrics)
			 //  config.registerMetricsConsumer(ExecutorMetricConsumer.class);
		if (local) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(conf.topologyName, config, topology);
			return 0;
		}

		StormSubmitter.submitTopology(conf.topologyName, config, topology);
		return 0;

	}
	
	
}
