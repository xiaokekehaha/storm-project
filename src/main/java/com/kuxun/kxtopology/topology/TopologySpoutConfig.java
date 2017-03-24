package com.kuxun.kxtopology.topology;

import java.io.Serializable;

public class TopologySpoutConfig implements Serializable{
    //请求jar文件服务的间隔时间
	public int timeIntervals = 2000;
    // topology spout 并发度
	public int parallelism = 1;

	 String topologyName;
	
	 String[] outputFields;
	 /**
	  * zookeeper集群地址
	  */
	public String zk;
	public Integer zkPort;
	public String zkRoot = "service";
	
	
	 public TopologySpoutConfig(String topologyName,String[] outputFields){
		  this.topologyName = topologyName;
		  this.outputFields = outputFields;
	 }
	 
}
