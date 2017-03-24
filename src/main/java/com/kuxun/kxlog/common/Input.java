package com.kuxun.kxlog.common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kuxun.kxlog.IContext;

/**
 * 在程序中，存在这样的一些程序传递过程产生的数据，
 * 例如，storm组件通信产生的Tuple，模型解析器由于
 * 解析日志产生的元数据等，这些数据都可以在这个接口中
 * 通过对应的方法取得，线程安全
 * @author dengzh
 */
public interface Input extends Serializable{
    /**
     * 获得当前storm组件的tuple消息
     * 
     * @return
     */
	public List getTupleInfo();
	 /**
	 * 通过key值获取设置在input实例中的属性
	 * 首先会通过查找查看是否存在key为可以transient的属性值
	 * 其次查找key对应的value为Serializable的属性值
	 * @param key 
	 * @return key对应的value值
	 */
	public Object getParameter(Object key);
	/**
	 * get the value of the key 'key'
	 * @param key
	 * @param serializable if true, find the serializable value stores in this instance, 
	 * otherwise, iterator the transient
	 *       to find the value 
	 * @return
	 */
	public Object getParameter(Object key,boolean serializable);
	/**
	 *  iterator the keys stores in this instance of Input class
	 *  @return keys iterator
	 */
	public Iterator keyIterator();
	/**
	 * put the map <key, value> into this instance of Input class
	 * note that the <key, value> is unserializable.  
	 * @param key key to store
	 * @param value value to store
	 */
	public void storePermanentData(Object key, Object value);
	
	/**
	 * put the map <key, value> into this instance of Input class.
	 * the difference between the storePermanentData method and storeTransientData
	 * is that  values stored in the latter will not be serialized.
	 * @param value value to store
	 */
	public void storeTransientData(Object key,Object value);
	
	/**
	 *  get the service context of this instance of Input class 
	 * @return  the service context
	 */
	public IContext getThisServiceContext();
	
	
	public String getStringParameter(Object key);
	
	/**
	 * 将 map存储到 input中
	 * @param map
	 */
	public void putAll(Map map);
	
	/**
	 * 将 key 移除 input环境中
	 * @param key
	 */
	public void removeKey(Object key);
	
	/**
	 * @return 当前对象缓存中存储的key，value键值对
	 */
	public Map getInputValue();
	
	/**
	 * 存储所有非序列化的键值对
	 * @param map
	 */
	public void putAllTransientValues(Map map);
}
