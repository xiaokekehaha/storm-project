package com.kuxun.kxlog;

import com.kuxun.kxlog.common.Input;
import com.kuxun.kxlog.common.KxException;

/**
 * 日志解析器
 * @author dengzh
 *
 */
public interface IParser {
   
	public void init(IParserConfig config) throws KxException;
	
	public void doParse(Input programData) throws KxException;
	
	public void destory() throws KxException;
}
