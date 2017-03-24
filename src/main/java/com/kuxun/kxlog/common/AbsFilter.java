package com.kuxun.kxlog.common;

import com.kuxun.kxlog.IFilter;
import com.kuxun.kxlog.IFilterConfig;
/**
 * 提供抽象类，方便用户专注于过滤器的逻辑
 * @author dengzh
 */
public abstract class AbsFilter implements IFilter {

	@Override
	public void init(IFilterConfig config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub
		
	}

}
