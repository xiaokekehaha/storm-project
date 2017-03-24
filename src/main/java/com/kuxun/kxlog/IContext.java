package com.kuxun.kxlog;

import java.io.Serializable;

/**
 * 在程序中，上下文运行环境
 * @author dengzh
 */
public interface IContext extends Serializable{
    
	/**
	 * store the Map <key, value> data into the context 
	 * @param key
	 * @param value
	 */
	public void contextData(Object key,Object value);
	
	/**
	 * get the value of the key 'key' stores in the context
	 * @param key
	 * @return
	 */
	public Object getDataInContext(Object key);
	
	/**
	 *  get this task index which generated this context instance
	 * @return  this task index
	 */
	public int getThisTaskIndex();
	
	/**
	 * get this total task size of storm component which generated the context instance
	 * @return total task size
	 */
	public int getThisTotalTaskSize();

	/**
	 * get the component id which generated the context instance
	 * @return the storm component id
	 */
	public String getThisSourceComponent();
}
