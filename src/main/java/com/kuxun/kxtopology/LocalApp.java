package com.kuxun.kxtopology;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务集成测试
 * 
 * @TODO JarClassLoader加载细节优化
 * @TODO Class 卸载
 * @author dengzh
 */
public class LocalApp {
	/**
	 * 参数信息
	 */
	public final static String CONFIGFILE = "config-file";
	public final static String TOPOLOGYTYPE = "topology-type";
	public final static String ZOOKEEPER = "zookeeper";
	public final static String TOPIC = "topic";
	public final static String TOPOLOGYNAME = "topology-name";
	public final static String KAFKASPOUT = "kafka-spout";
	public final static String JARSPOUT = "jar-spout";
	public final static String DATASOURCE = "datasource";
	public final static String FILTERBOLT = "filter-bolt";
	public final static String SERVICEBOLT = "service-bolt";
	public final static String WORKERNUM = "worker-num";
	/**
	 * true for本地模式调试,
	 * false for remote cluster
	 */
	public final static String LOCALCLUSTER = "local-cluster";
	/**
	 * 当为true时，则从用户指定的文件获取程序的设置入口，方便业务测试
	 */
	public final static String LOCAL = "local";

	public static void main(String[] args) throws Exception {
		/**
		 * local 模式
		 */

		StringBuffer buffer = new StringBuffer().append("config-file=配置文件地址").append(" ")
				.append("topology-type=[1|2|3|4]").append(" ").append("[zookeeper=xxxx topic=xxxx topology-name=xxx]")
				.append(" ").append("[kafka-spout=1|jar-spout=1|datasource=1]").append(" ").append("filter-bolt=2")
				.append(" ").append("service-bolt=3").append(" ").append("worker-num=1").append(" ")
				.append("local-cluster=true");

		if ((args == null || args.length == 0)) {
			System.out.println("########################################");
			System.out.println("########## possible args input##########");
			System.out.println("############# args format ##############");
			System.out.println(buffer.toString());
			System.out.println("########################################");
		}
		Map values = new HashMap();
		/**
		 * 采用哪种测试方式
		 */
		int topologyType = 1;
		values.put(LocalApp.TOPOLOGYTYPE, topologyType);
		values.put(LocalApp.TOPIC, "appyuqiang");
		values.put(LocalApp.LOCALCLUSTER, true);
		if ((args == null || args.length == 0)) {
			if (topologyType == 1 || topologyType == 3 || topologyType == 5) {
				String zk = "kooxoo90-116.kuxun.cn,kooxoo90-117.kuxun.cn,kooxoo90-118.kuxun.cn";
				values.put(LocalApp.ZOOKEEPER, zk);
				// 如果需要，明确kafka的 topic
				// 也可以设置 storm组件的并发参数
			}
		}
		for (String arg : args) {
			String[] keyvalue = arg.split("=");
			if (keyvalue.length != 2) {
				System.out.println("invalid key=value: " + arg);
				continue;
			}
			//
			values.put(keyvalue[0].toLowerCase(), keyvalue[1]);
		}

		boolean isLocal = Boolean.parseBoolean(String.valueOf(values.get(LocalApp.LOCALCLUSTER)));

		TopologyFactory.constructTopology(values, isLocal);
	}
}
