package com.kuxun.kxtopology.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;

import com.kuxun.kxlog.IServiceConfig;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;

/**
 * 解析业务逻辑入口
 * 
 * 
 *         <service name="" class=""> <params> <param name=""> </params> <param>
 *         <name> </name> <value> </value> <param> </params> </service>
 *
 */
public class ServiceConf implements Nodelet<IServiceConfig> {

	@Override
	public IServiceConfig process(Path path, Node node) throws KxException {
		final String thisServiceName = node.valueOf("@name");
		final String thisServiceClass = node.valueOf("@class");
		path.add("params");
		path.add("param");
		List<Node> nodes = node.getDocument().selectNodes(path.toString());
		final Map<String, Object> params = new HashMap<String,Object>();
		for (Node n : nodes) {
			String attributeName = n.valueOf("@name");
			String attributeValue = n.valueOf("@value");
			if (n instanceof Element) {
				Element elt = (Element) n;
				Element eltName = elt.element("name");
				Element eltValue = elt.element("value");
				attributeName = eltName != null ? eltName.getText()
						: attributeName;
				attributeValue = eltValue != null ? eltValue.getText()
						: attributeValue;
			}
			attributeValue = Utils.isNotBlank(attributeValue) ? attributeValue
					: n.getText();
			params.put(attributeName, attributeValue);
		}
		path.remove();
		path.remove();
		return new IServiceConfig() {
			private Map<String, Object> getMap() {
				return params;
			}

			@Override
			public Object getInitialValue(String key) {
				return params.get(key);
			}

			@Override
			public void setInitialValue(String key, Object obj) {
				getMap().put(key, obj);
			}

			@Override
			public void destory() {
				getMap().clear();
			}

			@Override
			public String getThisServiceClassName() {
				// TODO Auto-generated method stub
				return thisServiceClass;
			}

			@Override
			public String getThisServiceAlias() {
				// TODO Auto-generated method stub
				return thisServiceName;
			}
		};
	}

}
