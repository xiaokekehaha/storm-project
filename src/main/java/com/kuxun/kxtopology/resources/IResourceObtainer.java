package com.kuxun.kxtopology.resources;

import com.kuxun.kxtopology.jarservice.IJarManager;

public interface IResourceObtainer {
	/**
	 * 获取更改的jar信息列表资源地址
	 * @param params 请求参数
	 * @return
	 */
	public String getJarChangedListAddr(String...params);
	/**
	 * 获取下载jar信息的url
	 * @return
	 */
	public String getJarResourceAddr(String...params);
	/**
	 * 根据key获得属性值
	 * @param key
	 * @return                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	 */
	public String getResourceValue(String key);
	
	/**
	 * 
	 */
	
	public IJarManager getJarManager();
}
