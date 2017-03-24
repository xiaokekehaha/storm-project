package com.kuxun.kxlog;

import com.kuxun.kxlog.common.KxException;

public interface IServiceConfig {
  
	public String getThisServiceClassName();
	
	public String getThisServiceAlias();
	
	public void setInitialValue(String key, Object obj);

	public void destory() throws KxException;

	public Object getInitialValue(String key);
}
