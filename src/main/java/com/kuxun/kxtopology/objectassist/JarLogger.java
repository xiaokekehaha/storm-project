package com.kuxun.kxtopology.objectassist;

import org.slf4j.Logger;

import com.kuxun.kxtopology.cache.MapAware;

public class JarLogger implements MapAware<String, Logger> {
	
	private String service;
	
	private Logger logger;

	public String getKey() {
		// TODO Auto-generated method stub
		return service;
	}

	public Logger getValue() {
		// TODO Auto-generated method stub
		return logger;
	}
	
	public JarLogger(String service,Logger logger){
		   this.service = service;
		   this.logger = logger;
	}
}
