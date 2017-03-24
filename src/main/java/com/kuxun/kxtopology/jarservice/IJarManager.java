package com.kuxun.kxtopology.jarservice;
import java.util.List;

import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxtopology.pojo.JarConfig;

/**
 * 定义了在storm集群内，文件服务该有的操作
 * @author dengzh
 */
public interface IJarManager {
	public final static String CONFIG = "config.xml";
	
	public List<Jar> getChangableJarListFromJarServer(JarTopologyEvent params) throws KxException;

	public JarConfig loadJar(JarTopologyEvent jarTopology, String service) throws KxException;
}