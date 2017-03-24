package com.kuxun.kxlog;

import com.kuxun.kxlog.common.KxException;

/**
 * 自定义数据源的配置
 * 
 * @author dengzh
 */
public interface IDataSourceConfig {
	/**
	 * 得到数据源的class全名
	 * 
	 * @return 数据源的class全名
	 */
	public String getThisDataSourceClassName();

	/**
	 * 得到数据源的并发度
	 * 
	 * @return
	 */
	public int getThisDataSourceParallelism();

	/**
	 * 得到数据源的别名
	 * 
	 * @return
	 */
	public String getThisDataSourceAlias();
	
	/**
	 * 销毁该配置
	 */
	public void destory() throws KxException;

	/**
	 * 设置初始化参数
	 * @param key 初始化参数名称
	 * @param value 初始化参数值
	 */
	public void setInitialValue(String key, Object value);
	
	/**
	 * 获取键为key的初始化值
	 * @param key 
	 * @return 初始化值
	 */

	public Object getInitialValue(String key);

}
