package com.kuxun.kxlog;

import com.kuxun.kxlog.common.KxException;

public interface IParserConfig {

	public final static String DEFAULTPATTERN = "default";

	public String getThisParserName();
	
	public String getThisLogPatternByName(String name);
	
	public void setInitialValue(String key,Object obj);
	
	public Object getValue(String key);
	
	public String getThisLogParserClassName();

	public Object getInitialValue(String key);

	public void destory() throws KxException;
	
}
