package com.kuxun.kxtopology.topology;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.metric.api.IMetric;

public class CountMetric implements IMetric {

	private Map<String,Long> counts = new HashMap<String, Long>();
	
	@Override
	public Object getValueAndReset() {
		// TODO Auto-generated method stub
		return counts;
	}
	
	public void incr(String service){
		long count = counts.get(service);
		counts.put(service, ++count);
	}

}
