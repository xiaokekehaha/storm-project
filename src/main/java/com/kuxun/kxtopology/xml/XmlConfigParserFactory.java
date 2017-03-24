package com.kuxun.kxtopology.xml;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import com.kuxun.kxlog.IFilterConfig;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;

/**
 * 数据解析工厂，往Config中注入配置的数据信息
 * 
 */
public class XmlConfigParserFactory {
	Document document;

	public XmlConfigParserFactory(Document doc) {
		document = doc;
	}

	public String getValue(String attrValue, Node node) {
		String value = "";
		if (Utils.isNotBlank(attrValue))
			return attrValue;
		if (node != null) {
			String nodeValue = node.getStringValue();
			if (Utils.isNotBlank(nodeValue))
				return nodeValue;
		}
		return value;
	}

	/**
	 * @param servicename
	 *            名称
	 * @return 返回config获得的配置
	 */
	Path path = new Path(XmlConfigNodeDefinition.ROOTNODE);

	public Object parse(int servicename) {
		Nodelet nlt = null;
		switch (servicename) {
		case XmlConfigNodeDefinition.SERVICENAME:
			path.add(XmlConfigNodeDefinition.ServiceName.ROOTNODE);
			nlt = new ServiceNameNodelet();
			break;
		case XmlConfigNodeDefinition.FILTER:
			path.add(XmlConfigNodeDefinition.Filter.ROOTNODE);
			nlt = new FilterConf();
			break;
		case XmlConfigNodeDefinition.LOGPARSER:
			path.add(XmlConfigNodeDefinition.LogParser.ROOTNODE);
			nlt = new ParserConf();
			break;
		case XmlConfigNodeDefinition.LOGICSERVICE:
			path.add(XmlConfigNodeDefinition.Service.ROOTNODE);
			nlt = new ServiceConf();
			break;
		case XmlConfigNodeDefinition.DATASOURCE:
			path.add(XmlConfigNodeDefinition.Spout.ROOTNODE);
			nlt = new SpoutConf();
			break;
		case XmlConfigNodeDefinition.LOG:
			path.add(XmlConfigNodeDefinition.Log.ROOTNODE);
			nlt = new LoggerConf();
			break;
		}
		Object obj = parse(nlt);
		path.remove();
		if (obj != null) {
			if (servicename != XmlConfigNodeDefinition.FILTER) {
				obj = ((Object[]) obj)[0];
			} else if (servicename == XmlConfigNodeDefinition.FILTER) {
				Object[] objects = (Object[]) obj;
				IFilterConfig[] config = new IFilterConfig[objects.length];
				int i = 0;
				for (Object o : objects) {
					config[i++] = (IFilterConfig) o;
				}
				obj = config;
			}
		}
		return obj;
	}

	private Object parse(Nodelet nlt) {
		List<Node> nodes = document.selectNodes(path.toString());
		int len = nodes.size();
		Object[] retObj = null;
		if (len > 0)
			retObj = new Object[len];
		int i = 0;
		for (Node n : nodes) {
			try {
				retObj[i++] = nlt.process(path, n);
			} catch (KxException e) {
				e.printStackTrace();
			}
		}
		return retObj;
	}

}
