package com.kuxun.kxtopology;

import java.util.Map;
import java.util.UUID;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;

import com.kuxun.kxlog.utils.Utils;
import com.kuxun.kxtopology.topology.TopologyComponentId;
import com.kuxun.kxtopology.topology.TopologyDataSource;
import com.kuxun.kxtopology.topology.TopologyFileSpout;
import com.kuxun.kxtopology.topology.TopologyFilterBolt;
import com.kuxun.kxtopology.topology.TopologyServiceBolt;
import com.kuxun.kxtopology.topology.TopologySpoutConfig;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.InputDeclarer;
import backtype.storm.topology.TopologyBuilder;

public class TopologyFactory {

	public static void constructTopology(Map values,boolean local) throws AlreadyAliveException, InvalidTopologyException {
		Config conf = new Config();
		StormTopology topology = constructTopology(conf, values);
		
		if(topology ==null){
			System.out.println("topology = null,请检查参数设置，可能是zookeeper的集群地址没有设置");
			return;
		}
		if(local){
			System.out.println("########################################");
			System.out.println("########## LocalCluster Start ##########");
			System.out.println("########################################");
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology((String)values.get(LocalApp.TOPOLOGYNAME), conf, topology);
		}else{
			StormSubmitter.submitTopology((String)values.get(LocalApp.TOPOLOGYNAME), conf, topology);
		}
	}

	static StormTopology constructTopology(Config conf, Map values) {
		TopologyBuilder builder = new TopologyBuilder();
		Object obj = values.get(LocalApp.TOPOLOGYTYPE);
		int type = 1;
		if (obj != null)
			type = Integer.valueOf(obj + "");
		boolean success = false;
		if (type == 1 || type == 3) {
			boolean valid = vzk(values);
			if (!valid)
				return null;
			success = true;
			BrokerHosts hosts = new ZkHosts(String.valueOf(values.get(LocalApp.ZOOKEEPER)));
			String topic = String.valueOf(values.get(LocalApp.TOPIC));
			SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, "/" + topic, UUID.randomUUID().toString());
			spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
			KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
			builder.setSpout(TopologyComponentId.KAFkASPOUT, kafkaSpout,
					Integer.parseInt(String.valueOf(values.get(LocalApp.KAFKASPOUT))));

			String topologyName = (String) values.get(LocalApp.TOPOLOGYNAME);

			InputDeclarer<?> filterDeclarer = builder.setBolt(TopologyComponentId.FILTER,
					new TopologyFilterBolt(topologyName), Integer.parseInt(String.valueOf(values.get(LocalApp.FILTERBOLT))))
					.shuffleGrouping(TopologyComponentId.KAFkASPOUT);

			InputDeclarer<?> serviceDeclarer = builder.setBolt(TopologyComponentId.SERVICE,
					new TopologyServiceBolt(topologyName), Integer.parseInt(String.valueOf(values.get(LocalApp.SERVICEBOLT))))
					.shuffleGrouping(TopologyComponentId.FILTER);
			if(type ==1){
				//在这种模式中，不需要jar文件服务，从用户指定的config-file 或者 当前路径下 获取 config.xml文件 读取程序入口
				conf.put(LocalApp.LOCAL, true);
			}
			boolean vj = true;
			if (type == 3) {
				vj = vj(values);
				if (vj) {
					TopologySpoutConfig fileSpoutConf = new TopologySpoutConfig(topologyName, new String[] { "jars" });
					TopologyFileSpout fileSpout = new TopologyFileSpout(fileSpoutConf, topologyName);
					builder.setSpout(TopologyComponentId.FILESPOUT, fileSpout,
							Integer.parseInt(String.valueOf(values.get(LocalApp.JARSPOUT))));
					filterDeclarer.allGrouping(TopologyComponentId.FILESPOUT);
					serviceDeclarer.allGrouping(TopologyComponentId.FILESPOUT);
				}
			}

			success = success && vj;
		} else if (type == 2 || type == 4) {
			boolean vd = vd(values);
			if (vd) {
				success = true;
				String topologyName = (String) values.get(LocalApp.TOPOLOGYNAME);
				TopologySpoutConfig dataSourceConf = new TopologySpoutConfig(topologyName, new String[] { "service",
						"input" });
				TopologyDataSource dataSource = new TopologyDataSource(dataSourceConf, topologyName);
				builder.setSpout(TopologyComponentId.DATASOURCESPOUT, dataSource,
						Integer.parseInt(String.valueOf(values.get(LocalApp.DATASOURCE))));

				InputDeclarer<?> filterDeclarer = builder.setBolt(TopologyComponentId.FILTER,
						new TopologyFilterBolt(topologyName),
						Integer.parseInt(String.valueOf(values.get(LocalApp.FILTERBOLT)))).shuffleGrouping(
						TopologyComponentId.DATASOURCESPOUT);
				InputDeclarer<?> serviceDeclarer = builder.setBolt(TopologyComponentId.SERVICE,
						new TopologyServiceBolt(topologyName),
						Integer.parseInt(String.valueOf(values.get(LocalApp.SERVICEBOLT)))).shuffleGrouping(
						TopologyComponentId.FILTER);
				
				if(type==2){
					conf.put(LocalApp.LOCAL, true);
				}
				boolean vj = true;
				if (type == 4) {
					vj = vj(values);
					if (vj) {
						TopologySpoutConfig fileSpoutConf = new TopologySpoutConfig(topologyName,
								new String[] { "jars" });
						TopologyFileSpout fileSpout = new TopologyFileSpout(fileSpoutConf, topologyName);
						builder.setSpout(TopologyComponentId.FILESPOUT, fileSpout,
								Integer.parseInt(String.valueOf( values.get(LocalApp.JARSPOUT))));
						filterDeclarer.allGrouping(TopologyComponentId.FILESPOUT);
						serviceDeclarer.allGrouping(TopologyComponentId.FILESPOUT);
					}
				}

				success = success && vj;
			}
		} else if (type == 5) {
			boolean valid = vzk(values) && vj(values) && vd(values);
			if (!valid)
				return null;
			success = true;
			BrokerHosts hosts = new ZkHosts(String.valueOf(values.get(LocalApp.ZOOKEEPER)));
			String topic = String.valueOf(values.get(LocalApp.TOPIC));
			SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, "/" + topic, UUID.randomUUID().toString());
			spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
			KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
			builder.setSpout(TopologyComponentId.KAFkASPOUT, kafkaSpout,
					Integer.parseInt(String.valueOf(values.get(LocalApp.KAFKASPOUT))));

			String topologyName = (String) values.get(LocalApp.TOPOLOGYNAME);

			InputDeclarer<?> filterDeclarer = builder.setBolt(TopologyComponentId.FILTER,
					new TopologyFilterBolt(topologyName), Integer.parseInt(String.valueOf(values.get(LocalApp.FILTERBOLT))))
					.shuffleGrouping(TopologyComponentId.KAFkASPOUT);

			InputDeclarer<?> serviceDeclarer = builder.setBolt(TopologyComponentId.SERVICE,
					new TopologyServiceBolt(topologyName), Integer.parseInt(String.valueOf(values.get(LocalApp.SERVICEBOLT))))
					.shuffleGrouping(TopologyComponentId.FILTER);

			TopologySpoutConfig fileSpoutConf = new TopologySpoutConfig(topologyName, new String[] { "jars" });
			TopologyFileSpout fileSpout = new TopologyFileSpout(fileSpoutConf, topologyName);
			builder.setSpout(TopologyComponentId.FILESPOUT, fileSpout,
					Integer.parseInt(String.valueOf(values.get(LocalApp.JARSPOUT))));
			filterDeclarer.allGrouping(TopologyComponentId.FILESPOUT);
			serviceDeclarer.allGrouping(TopologyComponentId.FILESPOUT);

			TopologySpoutConfig dataSourceConf = new TopologySpoutConfig(topologyName, new String[] { "service",
					"input" });
			TopologyDataSource dataSource = new TopologyDataSource(dataSourceConf, topologyName);
			builder.setSpout(TopologyComponentId.DATASOURCESPOUT, dataSource,
					Integer.parseInt(String.valueOf(values.get(LocalApp.DATASOURCE))));
			filterDeclarer.shuffleGrouping(TopologyComponentId.DATASOURCESPOUT);
		}

		if (!success)
			return null;
		
        v(values);
        
        conf.setNumWorkers(Integer.parseInt(String.valueOf(values.get(LocalApp.WORKERNUM))));
        
        //可以配置storm其他参数信息
        conf.putAll(values);
        
		return builder.createTopology();
	}

	private static boolean v(Map values){
		Object worker = values.get(LocalApp.WORKERNUM);

		if (Utils.isBlank(worker)) {
			System.out.println("缺少worker num设置，该参数将被设置成1");
			values.put(LocalApp.WORKERNUM, 1);
		}
		return true;
	}
	
	private static boolean vj(Map values) {
		Object jarSpout = values.get(LocalApp.JARSPOUT);

		if (!Utils.isInt(jarSpout)) {
			System.out.println("jar spout参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.JARSPOUT, 1);
		}
		return true;
	}

	private static boolean vd(Map values) {
		// TODO Auto-generated method stub
		Object topologyName = values.get(LocalApp.TOPOLOGYNAME);

		if (Utils.isBlank(topologyName)) {
			System.out.println("缺少topology name设置，将采取默认名称为 test的 topology name");
			values.put(LocalApp.TOPOLOGYNAME, "test");
		}
		
		Object dataSource = values.get(LocalApp.DATASOURCE);

		if (!Utils.isInt(dataSource)) {
			System.out.println("dataSource参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.DATASOURCE, 1);
		}
		
		
		Object filterBolt = values.get(LocalApp.FILTERBOLT);

		if (!Utils.isInt(filterBolt)) {
			System.out.println("filterBolt参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.FILTERBOLT, 1);
		}

		Object serviceBolt = values.get(LocalApp.SERVICEBOLT);

		if (!Utils.isInt(serviceBolt)) {
			System.out.println("serviceBolt参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.SERVICEBOLT, 1);
		}
		return true;
	}

	/**
	 * 检测zookeeper, topic topologyName, 以及各种组件的 并发设置
	 * 
	 * @param values
	 * @return
	 */
	private static boolean vzk(Map values) {
		Object zk = values.get(LocalApp.ZOOKEEPER);
		if (Utils.isBlank(zk)) {
			System.out.println("必须显式声明kafka的zookeeper集群地址");
			return false;
		}

		Object topic = values.get(LocalApp.TOPIC);
		if (Utils.isBlank(topic)) {
			System.out.println("缺少topic设置，kafka spout将读取默认的名称为 test的 topic");
			values.put(LocalApp.TOPIC, "test");
		}

		Object topologyName = values.get(LocalApp.TOPOLOGYNAME);

		if (Utils.isBlank(topologyName)) {
			System.out.println("缺少topology name设置，将采取默认名称为 test的 topology name");
			values.put(LocalApp.TOPOLOGYNAME, "test");
		}

		Object kafkaSpout = values.get(LocalApp.KAFKASPOUT);

		if (!com.kuxun.kxlog.utils.Utils.isInt(kafkaSpout)) {
			System.out.println("kafkaspout参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.KAFKASPOUT, 1);
		}

		Object filterBolt = values.get(LocalApp.FILTERBOLT);

		if (!Utils.isInt(filterBolt)) {
			System.out.println("filterBolt参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.FILTERBOLT, 1);
		}

		Object serviceBolt = values.get(LocalApp.SERVICEBOLT);

		if (!Utils.isInt(serviceBolt)) {
			System.out.println("serviceBolt参数设置不正确，该参数将被设置成1");
			values.put(LocalApp.SERVICEBOLT, 1);
		}
		return true;
	}
}
