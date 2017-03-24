package com.kuxun.kxtopology.xml;

import org.dom4j.Node;

/**
 * 解析<service-name/> 节点
 */
import com.kuxun.kxlog.common.KxException;

/**
 * <service-name> </service-name>
 *
 */
public class ServiceNameNodelet implements Nodelet<String> {

	@Override
	public String process(Path path,Node node) throws KxException {
		return node.getText();
	}

}
