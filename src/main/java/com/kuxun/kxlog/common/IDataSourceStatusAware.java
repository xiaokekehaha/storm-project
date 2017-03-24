package com.kuxun.kxlog.common;
/**
 *用于client获取自定义数据源的状态
 * 
 * @author dengzh
 */
public interface IDataSourceStatusAware {
  /**
   * 获取自定义数据源的状态
   * @return
   */
	public DataSourceStatus getThisDataSourceStatus();
}
