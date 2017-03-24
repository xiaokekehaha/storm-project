package com.kuxun.kxtopology.xml;


import org.dom4j.Node;

import com.kuxun.kxlog.common.KxException;

/**
 *  xml解析接口
 */
public interface Nodelet<T>{
	/**
	 * @param node  当前的node
	 * @param path  xpath路径
	 * @throws KxException
	 */
	public T process(Path path,Node node) throws KxException;
}
