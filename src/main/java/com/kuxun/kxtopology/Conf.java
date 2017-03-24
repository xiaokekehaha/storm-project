package com.kuxun.kxtopology;

/**
 * 拓扑参数配置，由用户上传topology时捕获的参数的设置
 * @author dengzh
 */
public class Conf {
	/**
	 * 拓扑名称
	 */
	String topologyName;
	/**
	 * 工作进程数
	 */
	int  workerNum;
	
	/**
	 * 自定义数据源并发量
	 */
	int dataSourceNum;
	
	/**
	 * 文件节点并发量
	 */
	int fileSpoutNum;
	
	/**
	 * 过滤器并发量
	 */
	int filterNum;
	
	/**
	 * 业务服务入口并发量
	 */
	int serviceNum;
	
	/**
	 * kafkaspout并发量
	 */
	int kafkaSpoutNum;
	
	/**
	 * zookeeper集群地址
	 */
    String zookeeper;
	
    /**
     * kafka topic
     */
	String topic;
	
	boolean enableMetrics = false;
	
	/**
	 * 是否需要文件服务,默认为true 即需要
	 */
	boolean enableFileService = true;
	
}
