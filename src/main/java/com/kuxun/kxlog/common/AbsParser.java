package com.kuxun.kxlog.common;

import com.kuxun.kxlog.IParser;
import com.kuxun.kxlog.IParserConfig;

public abstract class AbsParser implements IParser {
    
	protected IParserConfig config;
	
	@Override
	public final  void init(IParserConfig config) {
		// TODO Auto-generated method stub
		this.config = config;
		initParser(this.config);
	}

	
	
	protected abstract void initParser(IParserConfig config);
	
	public String getLogPattern(String name){
		if(name == null)
			return null;
		return config.getThisLogPatternByName(name);
	}
	
	public String getDefaultLogPattern(){
		return getLogPattern(IParserConfig.DEFAULTPATTERN);
	}
	@Override
	public void destory() throws KxException{
		// TODO Auto-generated method stub
          config.destory();
	}

	
}
