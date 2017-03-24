package com.kuxun.kxlog.common;

import com.kuxun.kxlog.IDataSource;
import com.kuxun.kxlog.IDataSourceConfig;

public abstract class AbsDataSource implements IDataSource {

	IDataSourceConfig conf ;
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public final void init(IDataSourceConfig config) {
		// TODO Auto-generated method stub
        conf = config;
        initConf(conf);
	}

	protected abstract void initConf(IDataSourceConfig config);
	
	
	public Object getParameter(String name){
	    return conf.getInitialValue(name);
	}
	
	@Override
	public void ack(Object msgId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail(Object msgId) {
		// TODO Auto-generated method stub

	}

}
