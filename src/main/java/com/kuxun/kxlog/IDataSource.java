package com.kuxun.kxlog;

import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;

/**
 * 数据发射源，当业务场景需要自定义数据来源，不需要从kafka读取日志信息时，只需要实现该接口，
 * 并显式进行声明配置，也可以随时关闭该数据源
 * 一旦数据源关闭，storm将丢失原来的信息，导致无法在将原数据源开启
 * @author dengzh
 */

public interface IDataSource {
	/**
	 * 数据源 关闭时 调用
	 */
	public void cleanup() throws KxException;
	
	/**
	 * 数据源 生产数据接口
	 * 如果希望后面的过滤器或业务逻辑执行器获取生成的数据，
	 * 可以将数据以key-value的形式存储在input中
	 * 在业务中，可以根据msgId追踪你这条消息
	 * @return if not null, the storm will track this message and will replay when it fails.
	 */
	public Object message(Input input,Object msgId) throws KxException;
     /**
      * 配置好的信息可以在request中取得
      * @param spoutConf 配置信息
      * @throws SDKException
      */
	public void init(IDataSourceConfig config) throws KxException;
	/**
	 * 数据处理成功时调用 
	 * @param msgId
	 */
	public void ack(Object msgId) throws KxException;
	
	/**
	 * 数据处理失败时候调用
	 * @param msgId
	 * @throws SDKException
	 */
	public void fail(Object msgId) throws KxException;

}
