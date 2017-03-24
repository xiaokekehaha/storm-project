package com.kuxun.kxlog.common;

/**
 * 自定义数据源的状态
 * 定义了四种数据源的状态
 * NEW，新建，该数据源处于可以用，但未开始发射数据
 * ACTIVE，活动，该数据源处于正在发射数据状态
 * DEACTIVE， 暂停，该数据源可用，但没发射数据
 * DEPROCATED，废弃，该数据源处于不可用状态
 * @author dengzh
 */
public enum DataSourceStatus {
		NEW/*数据源新建*/,
	    ACTIVE/*数据源处于活动状态*/,
		DEACTIVE/*数据源处于暂停状态*/,
		DEPROCATED/*数据源处于废弃状态*/
}
