package com.kuxun.kxtopology;

import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;

/**
 * Hello world!
 * 可以拓展的问题：
 *    1. 用户可以不需要文件服务管理，一样可以使用本项目中包含的设计，实现自己的业务. done
 *    2. 在第一种情况下，用户是否可以根据需要，自定义自己的拓扑结构，包括增加拓扑节点。
 *    3. 若jar文件服务出现问题，比如文件服务崩溃，此时该怎么处理?
 *    4. 怎样增加本地调试模式
 *    @TODO 存堵 有东西过期?
 *    @TODO 可以想业务逻辑上传入一份storm的配置信息
 *    
 *    this class can be used to test how the framework works!
 *    
 *    @TODO 统计不同的业务消息数
 *    @TODO 如何处理消息与实例之间的空隙而导致的消息未处理的情况
 *    @TODO 完善日志处理
 *    @TODO
 *
 *    sql on storm 实现一个半产品
 *    // 用户如何自定义自己的逻辑
 */
public class App {

	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
		boolean test = false;
		Conf conf = new Conf();
		boolean local = false;
		if (args == null || args.length == 0) {
			conf.dataSourceNum = 1;
			conf.fileSpoutNum = 1;
			conf.filterNum = 3;
			conf.serviceNum = 4;
			conf.topologyName = "test";
			conf.workerNum = 2;
			if (!test) {
				conf.zookeeper = "kooxoo90-116.kuxun.cn,kooxoo90-117.kuxun.cn,kooxoo90-118.kuxun.cn";
				conf.topic = "testapp";
				conf.kafkaSpoutNum = 3;
			}

			System.out.println("########################################");
			System.out.println("########## LocalCluster Start ##########");
			System.out.println("########################################");
			local = true;
		} else {
			int len = args.length;
			StringBuffer buffer = new StringBuffer("args usage:");
			buffer.append("topologyName").append(" ").append("workerNum").append(" ").append("dataSourceNum")
					.append(" ").append("fileSpoutNum").append(" ").append("filterNum").append(" ")
					.append("serviceNum").append(" ");
			if (test && len != 6) {
				System.out.println("########################################");
				System.out.println("##########     Wrong args     ##########");
				System.out.println(buffer.toString());
				System.out.println("########################################");
				return;
			} else if (!test && len != 9) {
				System.out.println("########################################");
				System.out.println("##########     Wrong args     ##########");
				buffer.append("kafkaSpoutNum").append(" ").append("zookeeper").append(" ").append("topic");
				System.out.println(buffer.toString());
				System.out.println("########################################");
				return;
			}

			conf.topologyName = args[0];
			conf.workerNum = Integer.parseInt(args[1]);
			conf.dataSourceNum = Integer.parseInt(args[2]);
			conf.fileSpoutNum = Integer.parseInt(args[3]);
			conf.filterNum = Integer.parseInt(args[4]);
			conf.serviceNum = Integer.parseInt(args[5]);

			if (!test) {
				conf.kafkaSpoutNum = Integer.parseInt(args[6]);
				conf.zookeeper = args[7];
				conf.topic = args[8];
			}

		}

		int start = test ? TopologyConstructor.constructTopologyTest(conf, local)
				: TopologyConstructor.constructTopology(conf, local);

	}
}
