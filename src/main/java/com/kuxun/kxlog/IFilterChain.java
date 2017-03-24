package com.kuxun.kxlog;

import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;

/**
 * 过滤器链条
 * @author dengzh
 */
public interface IFilterChain {
	
	 public void doFilter(Input input) throws KxException;

}
