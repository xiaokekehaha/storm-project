package com.kuxun.kxtopology.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;

import com.kuxun.kxlog.IDataSourceConfig;
import com.kuxun.kxlog.common.KxException;
import com.kuxun.kxlog.utils.Utils;

/**
 *  解析自定义数据源
 * <spout name="" class="" parallelism="">
 *     <params>
 *       <param name=""> </param>
 *       <param>
 *          <name> </name>
 *          <value> </value>
 *        </param>
 *      </params>
 *  </spout>
 */
public class SpoutConf implements Nodelet<IDataSourceConfig> {
	@Override
	public IDataSourceConfig process(Path path, Node node)
			throws KxException {
		// TODO Auto-generated method stub
		final String name = node.valueOf("@name");
		final String className = node.valueOf("@class");
		String strValue= node.valueOf("@parallelism");
		int para = 1;
		if(Utils.isNotBlank(strValue)){
			para = Integer.parseInt(strValue);
		}
		final int parallelism  = para;
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
				Element eltValue  =elt.element("value");
				attributeName = eltName!=null?eltName.getText():attributeName;
				attributeValue=eltValue!=null?eltValue.getText():attributeValue;
			}
			attributeValue = Utils.isNotBlank(attributeValue) ? attributeValue
					: n.getText();
             params.put(attributeName, attributeValue);
		}
		
		path.remove();
		path.remove();
		return new IDataSourceConfig() {
			
			private Map<String,Object> getMap(){
				return params;
			}
			@Override
			public Object getInitialValue(String key){
				return getMap().get(key);
			}
			@Override
			public void setInitialValue(String key, Object value){
				getMap().put(key, value);
			}
			@Override
			public void destory(){
				getMap().clear();
			}
			@Override
			public int getThisDataSourceParallelism() {
				// TODO Auto-generated method stub
				return parallelism;
			}
			
			@Override
			public String getThisDataSourceClassName() {
				// TODO Auto-generated method stub
				return className;
			}
			
			@Override
			public String getThisDataSourceAlias() {
				// TODO Auto-generated method stub
				return name;
			}
		};
	}

}
