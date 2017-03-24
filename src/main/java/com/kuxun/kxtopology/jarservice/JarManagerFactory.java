package com.kuxun.kxtopology.jarservice;

import java.util.Map;

import com.kuxun.kxtopology.LocalApp;
import com.kuxun.kxtopology.jarservice.JarService.JarManagerTmp;

public class JarManagerFactory {

	public static JarManagerTmp createManager(Map conf) {
		IJarManager manager = null;
		Object  obj = conf.get(LocalApp.LOCAL);
		boolean isLocal = false;
		if(obj instanceof Boolean){
			isLocal = (Boolean)obj;
		}
		// 不需要 文件服务
		
		if(isLocal){
			 manager = new JarManagerLocalImpl(conf);
		}else{
			 manager = new JarManagerImpl();
		}
		
		JarManagerTmp tmp = new JarManagerTmp();
		tmp.isLocal = isLocal;
		tmp.manager = manager;
		return tmp;
	}

}
