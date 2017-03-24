package com.kuxun.kxtopology.topology;

import com.kuxun.kxtopology.jarservice.Jar;

/**
 * 处理发生的jar文件
 *  1. 新增 2. 删除  3. 更新
 * @author dengzh
 */
public interface IChangedJarAware {
	/**
	 * @param jar
	 *            发生变更的jar文件
	 */
	public void doChangedJars(Jar jar);
}
