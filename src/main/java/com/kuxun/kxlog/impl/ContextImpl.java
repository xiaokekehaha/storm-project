package com.kuxun.kxlog.impl;

import java.util.HashMap;
import java.util.Map;

import com.kuxun.kxlog.IContext;

/**
 * 上下文环境具体实现
 * 
 * @author dengzh
 */
public class ContextImpl implements IContext {

	private Map contextData = new HashMap();

	private int taskIndex;
	
	private int totalTask;
	
	private String sourceComponent;
	
	
	public ContextImpl(Map values){
		contextData.putAll(values);
	}
	
	public ContextImpl(){
		
	}
	@Override
	public synchronized void contextData(Object key, Object value) {
		contextData.put(key, value);
	}

	@Override
	public Object getDataInContext(Object key) {
		return contextData.get(key);
	}

	@Override
	public int getThisTaskIndex() {
		// TODO Auto-generated method stub
		return taskIndex;
	}

	@Override
	public int getThisTotalTaskSize() {
		// TODO Auto-generated method stub
		return totalTask;
	}

	
	public void setTaskIndex(int taskIndex) {
		this.taskIndex = taskIndex;
	}

	
	public void setTotalTask(int totalTask) {
		this.totalTask = totalTask;
	}
  
	@Override
	public String getThisSourceComponent() {
		return sourceComponent;
	}

	public void setSourceComponent(String sourceComponent) {
		this.sourceComponent = sourceComponent;
	}
	
 
}
