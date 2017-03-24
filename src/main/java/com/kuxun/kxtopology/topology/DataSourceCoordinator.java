package com.kuxun.kxtopology.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import backtype.storm.task.TopologyContext;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxtopology.pojo.JarConfig;
import com.kuxun.kxtopology.topology.TopologyDataSource.SpoutConf;

/**
 * 自定义数据源的并发控制
 * 
 * @author dengzh
 */
public class DataSourceCoordinator {

	TopologyContext context;

	public DataSourceCoordinator(TopologyContext context) {
		this.context = context;
	}

	public List<SpoutConf> calculateSpoutForTask(Map<String, SpoutConf> spouts) {

		Collection<SpoutConf> collection = spouts.values();
		List<SpoutConf> retList = new ArrayList<SpoutConf>();
		// 获取总任务数量
	    int taskIndex = context.getThisTaskIndex();
		for (SpoutConf spout : collection) {
			JarConfig conf = spout.conf;
			if (conf == null)
				continue;
            IDataSourceConfig dataSourceConf = conf.getThisJarDataSource();
            int parallelism = dataSourceConf.getThisDataSourceParallelism();
            if(taskIndex<parallelism){
            	    retList.add(spout);
            }
		}

		return retList;
	}
}
