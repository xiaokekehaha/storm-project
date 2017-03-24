package com.kuxun.kxtopology.jarservice;

import java.util.List;

import com.kuxun.kxtopology.pojo.JarConfig;
/**
 * 
 * @author dengzh
 */
public interface JarAware {
	/**
	 * notify the invocation the changed jar list
	 * @param changedJars
	 */
	public void notifyJarChange(List<Jar> changedJars);
	/**
	 * when jar loaded into JVM, notify the invocation of jar config
	 */
	public void notifyJarConfig(JarConfig config);
}
